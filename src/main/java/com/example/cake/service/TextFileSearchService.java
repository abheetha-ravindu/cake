package com.example.cake.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service

public class TextFileSearchService {
    private List<String> dataList = new ArrayList<>();

    @PostConstruct
    public void loadDataFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource("src/main/resources/static/item/item.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                dataList.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<String> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }


        String lowerCaseKeyword = keyword.toLowerCase();

        return dataList.stream()
                .filter(line -> line.toLowerCase().contains(lowerCaseKeyword))
                .collect(Collectors.toList());
    }

}