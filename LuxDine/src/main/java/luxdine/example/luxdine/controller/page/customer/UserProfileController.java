package luxdine.example.luxdine.controller.page.customer;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.user.dto.request.ChangePasswordRequest;
import luxdine.example.luxdine.domain.user.dto.request.ProfileUpdateRequest;
import luxdine.example.luxdine.domain.user.dto.response.VipProgressResponse;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.entity.VipTiers;
import luxdine.example.luxdine.service.user.UserService;
import luxdine.example.luxdine.service.user.VipTierService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileController {

    final UserService userService;
    final VipTierService vipTierService;

    @GetMapping("/profile")
    public String view(@RequestParam(defaultValue="profile") String tab,
                       Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        List<VipTiers> tiers = vipTierService.findAllOrderByMinPoints();

        VipProgressResponse progress = vipTierService.compute(user, tiers);

        model.addAttribute("name", user.getLastName() + " " + user.getFirstName());
        model.addAttribute("user", user);
        model.addAttribute("vipTiers", tiers);
        model.addAttribute("tab", tab);
        model.addAttribute("progress", progress);
        model.addAttribute("username", principal.getName());
        return "customer/profile/user_profile";
    }

}