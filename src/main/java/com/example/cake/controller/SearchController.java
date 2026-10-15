package com.example.cake.controller;


import com.example.cake.service.TextFileSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
    @RequestMapping("/api")
    public class SearchController {

        @Autowired
        private TextFileSearchService searchService;


        @GetMapping("/search")
        public List<String> searchData(@RequestParam String keyword) {
            return searchService.search(keyword);
        }
    }

