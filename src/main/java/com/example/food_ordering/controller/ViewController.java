package com.example.food_ordering.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/signin")
    public String signin() {
        return "auth/signin";
    }

    @GetMapping("/signup")
    public String signup() {
        return "auth/signup";
    }

    @GetMapping("/404")
    public String error404() {
        return "404";
    }

    @GetMapping("/customer/menu")
    public String menu() {
        return "customer/menu";
    }

    @GetMapping("/customer/cart")
    public String cart() {
        return "customer/cart";
    }

    @GetMapping("/customer/payment")
    public String payment() {
        return "customer/payment";
    }

    @GetMapping("/customer/orders")
    public String orders() {
        return "customer/orders";
    }

    @GetMapping("/customer/tracking")
    public String tracking() {
        return "customer/tracking";
    }

    @GetMapping("/customer/notifications")
    public String notifications() {
        return "customer/notifications";
    }

    @GetMapping("/staff/orders")
    public String staffOrders() {
        return "staff/staff-orders";
    }

    @GetMapping("/manager/foods")
    public String managerFoods() {
        return "manager/manager-foods";
    }

    @GetMapping("/manager/staff")
    public String managerStaff() {
        return "manager/manager-staff";
    }
}
