package luxdine.example.luxdine.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {
    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;

    /** AVAILABLE -> RESERVED khi vào cửa sổ [-1h, +1h], chỉ reservation ONLINE */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void updateTableStatusBasedOnReservationTime() {
        List<Reservations> list = reservationRepository.findConfirmedOnlineReservationsForActivation();
        if (list.isEmpty()) {
            log.info("Activation: checked=0, updated=0");
            return;
        }

        int updated = 0;
        List<Long> changedTableIds = new ArrayList<>();

        for (Reservations r : list) {
            try {
                if (r.getTable() == null) continue;
                Tables t = r.getTable();

                // Chỉ đổi AVAILABLE -> RESERVED khi vào cửa sổ giờ
                if (t.getStatus() == TableStatus.AVAILABLE && isReservationTimeReached(r)) {
                    t.setStatus(TableStatus.RESERVED);
                    tableRepository.save(t);
                    updated++;
                    changedTableIds.add(t.getId());
                }
            } catch (Exception ex) {
                log.error("Activation error for reservation {}: {}", r.getId(), ex.getMessage(), ex);
            }
        }

        log.info("Activation: checked={}, updated={}", list.size(), updated);
        if (log.isDebugEnabled() && !changedTableIds.isEmpty()) {
            log.debug("Activation tables updated: {}", changedTableIds);
        }
    }

    /** RESERVED -> AVAILABLE nếu quá +1h sau reservationTime (no-show), chỉ reservation ONLINE */
    @Scheduled(fixedRate = 30000, initialDelay = 15000)
    @Transactional
    public void releaseTablesForNoShow() {
        List<Reservations> list = reservationRepository.findOnlineReservationsWithReservedTables();
        if (list.isEmpty()) {
            log.info("Release: checked=0, updated=0");
            return;
        }

        int released = 0;
        List<Long> changedTableIds = new ArrayList<>();

        for (Reservations r : list) {
            try {
                if (r.getTable() == null) continue;
                Tables t = r.getTable();

                if (t.getStatus() == TableStatus.RESERVED && isReservationWindowExpired(r)) {
                    t.setStatus(TableStatus.AVAILABLE);
                    tableRepository.save(t);
                    released++;
                    changedTableIds.add(t.getId());
                    tryMarkNoShow(r);
                }
            } catch (Exception ex) {
                log.error("Release error for reservation {}: {}", r.getId(), ex.getMessage(), ex);
            }
        }

        log.info("Release: checked={}, updated={}", list.size(), released);
        if (log.isDebugEnabled() && !changedTableIds.isEmpty()) {
            log.debug("Release tables updated: {}", changedTableIds);
        }
    }

    /** Tuỳ chọn: đánh dấu no-show (ở đây mình set CANCELLED nếu đang CONFIRMED) */
    private void tryMarkNoShow(Reservations r) {
        try {
            if (r.getStatus() == ReservationStatus.CONFIRMED) {
                r.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(r);
            }
        } catch (Exception ignored) { /* không chặn luồng chính */ }
    }

    /** Hết cửa sổ: now > reservationTime + 1h */
    private boolean isReservationWindowExpired(Reservations r) {
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime rvUtc = r.getReservationDate().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime deadline = rvUtc.plusHours(1);
        return nowUtc.isAfter(deadline);
    }

    /** Trong cửa sổ [-1h, +1h] của reservationTime (inclusive) */
    private boolean isReservationTimeReached(Reservations r) {
        if (r == null || r.getReservationDate() == null) return false;
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime rvUtc = r.getReservationDate().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime start = rvUtc.minusHours(1);
        OffsetDateTime end   = rvUtc.plusHours(1);
        return !nowUtc.isBefore(start) && !nowUtc.isAfter(end);
    }
}
