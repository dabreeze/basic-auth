package com.cdl.basicauth.controller;import com.cdl.basicauth.service.CustomeUserDetailsService;import org.springframework.http.ResponseEntity;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.RequestMapping;import org.springframework.web.bind.annotation.RestController;@RestController@RequestMapping("/api/v1")public class HomeController {    private CustomeUserDetailsService customeUserDetailsService;    public HomeController(CustomeUserDetailsService customeUserDetailsService) {        this.customeUserDetailsService = customeUserDetailsService;    }    @GetMapping    @PreAuthorize("/home")    public ResponseEntity<?> home(){        return ResponseEntity.ok().body("Welcome to my home page");    }}