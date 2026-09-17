package vn.iotstar.controller;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class PageController {
 @Value("${app.mode}") private String mode;
 @ModelAttribute public void common(Model model) {model.addAttribute("mode",mode);}
 @GetMapping({"/","/home"}) public String entry() {return "redirect:/admin/categories";}
 @GetMapping("/admin/categories") public String categories(Model model) {model.addAttribute("page","categories");return "index";}
 @GetMapping("/admin/products") public String products(Model model) {model.addAttribute("page","products");return "index";}
}
