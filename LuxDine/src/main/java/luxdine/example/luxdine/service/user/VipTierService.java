package luxdine.example.luxdine.service.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.user.dto.response.VipProgressResponse;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.entity.VipTiers;
import luxdine.example.luxdine.domain.user.repository.VipTierRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VipTierService {
    VipTierRepository vipTierRepository;

    public List<VipTiers> findAllOrderByMinPoints(){
        return vipTierRepository.findAll(Sort.by("requiredPoints"));
    }

    public VipProgressResponse compute(User user, List<VipTiers> tiersSortedAsc) {
        List<VipTiers> tiers = Optional.ofNullable(tiersSortedAsc).orElse(List.of())
                .stream()
                .sorted(Comparator.comparingInt(this::safeReq))
                .toList();
        int userPts = Math.max(0, Optional.ofNullable(user.getRewardPoints()).orElse(0));

        VipTiers curr = (user.getVipTier() != null)
                ? user.getVipTier()
                : (tiers.isEmpty() ? null : tiers.get(0));

        VipTiers next = (curr == null) ? null
                : tiers.stream()
                .filter(t -> safeReq(t) > safeReq(curr))
                .findFirst()
                .orElse(null);

        int from = (curr != null) ? Math.max(0, safeReq(curr)) : 0;
        Integer nextReq = (next != null ? safeReq(next) : null);

        int pct, to;
        if (nextReq == null) {
            // top-tier
            to = from;         // hoặc giữ nguyên from, không tạo mốc ảo
            pct = 100;         // luôn đầy thanh
        } else {
            to = nextReq;
            int denom = Math.max(1, to - from);
            pct = (int) Math.max(0, Math.min(100,
                    Math.round(((userPts - from) * 100.0) / denom)));
        }

        int remaining = (nextReq == null) ? 0 : Math.max(0, to - userPts);

        return VipProgressResponse.builder()
                .progressPct(pct)
                .fromPoints(from)
                .toPoints(to)
                .nextTierName(next != null ? next.getTierName() : null)
                .vipEnabled(curr != null)
                .remainingToNext(remaining)
                .build();
    }

    private int safeReq(VipTiers tier) {
        return Math.max(0, Optional.of(tier.getRequiredPoints()).orElse(0));
    }
}
