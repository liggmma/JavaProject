package luxdine.example.luxdine.controller.page.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.common.util.OtpSessionKeys;
import luxdine.example.luxdine.domain.catalog.entity.Bundles;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.user.dto.request.UserCreationRequest;
import luxdine.example.luxdine.service.auth.AuthenticationService;
import luxdine.example.luxdine.service.auth.EmailOtpService;
import luxdine.example.luxdine.service.catalog.BundleService;
import luxdine.example.luxdine.service.catalog.ItemsService;
import luxdine.example.luxdine.service.content.BannerService;
import luxdine.example.luxdine.service.feedback.FeedBackService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthController {

    final BannerService bannerService;
    final ItemsService itemsService;
    final BundleService bundleService;
    final FeedBackService feedBackService;
    final UserService userService;
    final EmailOtpService emailOtpService;
    final AuthenticationService authenticationService;

    @GetMapping("/")
    public String rootRedirect() { return "redirect:/home"; }

    @GetMapping("/login")
    public String login(CsrfToken token) {
        token.getToken();
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        return "auth/login";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails user, Model model,
                       @ModelAttribute("message") String message) {
        model.addAttribute("banners", bannerService.getAllActiveBanner());
        model.addAttribute("items", itemsService.getAllBestSellerItems());
        model.addAttribute("feedbacks", feedBackService.get10RecentFeedBacks());
        List<Items> allItems = itemsService.getAllItemsByVisibility();
        List<Bundles> allBundles = bundleService.getAllActiveBundlesEntity();
        if (user != null) model.addAttribute("username", user.getUsername());
        if (message != null && !message.isEmpty()) model.addAttribute("message", message);
        return "customer/home/home";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        model.addAttribute("user", new UserCreationRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute("user") UserCreationRequest req,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        if (req.getPassword() == null || !req.getPassword().equals(confirmPassword)) {
            ra.addFlashAttribute("message", "Mật khẩu xác nhận không khớp!");
            return "redirect:/register";
        }
        if (req.getPassword().length() < 6) {
            ra.addFlashAttribute("message", "Mật khẩu tối thiểu 6 kí tự");
            return "redirect:/register";
        }
        if (userService.existsByUsername(req.getUsername())) {
            ra.addFlashAttribute("message", "Tên đăng nhập đã tồn tại!");
            return "redirect:/register";
        }
        if (userService.existsByEmail(req.getEmail())) {
            ra.addFlashAttribute("message", "Email đã tồn tại!");
            return "redirect:/register";
        }

        session.setAttribute(OtpSessionKeys.PENDING_REG, req);
        return "redirect:/auth/verify";
    }

    @GetMapping("/auth/verify")
    public String showVerifyPage(HttpSession session, Model model) {
        UserCreationRequest pending = (UserCreationRequest) session.getAttribute(OtpSessionKeys.PENDING_REG);
        if (pending == null) return "redirect:/register";
        model.addAttribute("email", pending.getEmail());
        emailOtpService.sendOtp(pending.getEmail(), session);  // rate-limit nằm trong service
        return "auth/verify-email";
    }

    @GetMapping("/auth/verify/finish")
    public String finishRegistration(HttpSession session,
                                     RedirectAttributes ra,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        UserCreationRequest pending = (UserCreationRequest) session.getAttribute(OtpSessionKeys.PENDING_REG);
        if (pending == null) return "redirect:/register";

        Boolean verified = (Boolean) session.getAttribute(OtpSessionKeys.VERIFIED);
        String verifiedEmail = (String) session.getAttribute(OtpSessionKeys.VERIFIED_EMAIL);
        if (!Boolean.TRUE.equals(verified) || verifiedEmail == null ||
                !verifiedEmail.equalsIgnoreCase(pending.getEmail())) {
            ra.addFlashAttribute("message", "Vui lòng xác thực OTP trước.");
            return "redirect:/auth/verify";
        }

        try {
            String username = userService.registerVerified(pending); // << trả về username
            // dọn session OTP/pending
            session.removeAttribute(OtpSessionKeys.PENDING_REG);
            session.removeAttribute(OtpSessionKeys.VERIFIED);
            session.removeAttribute(OtpSessionKeys.VERIFIED_EMAIL);

            // auto-login
            authenticationService.loginAfterRegisterWithoutPassword(username, request, response);
            return "redirect:/home";
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("message", ex.getMessage());
            return "redirect:/register";
        }
    }


}
