package luxdine.example.luxdine.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VipProgressResponse {
    private int progressPct;     // 0..100
    private int fromPoints;
    private int toPoints;        // = fromPoints khi top-tier
    private String nextTierName; // null nếu top-tier
    private boolean vipEnabled;
    private int remainingToNext; // 0 khi top-tier
}

