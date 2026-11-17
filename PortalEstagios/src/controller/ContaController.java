package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContaController {
    @GetMapping("/login")
    public String login() { return "redirect:/login.html"; }

    @GetMapping("/")
    public String inicio() { return "redirect:/conta.html"; }
}
