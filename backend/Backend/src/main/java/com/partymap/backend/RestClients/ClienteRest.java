package com.partymap.backend.RestClients;


import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name ="ClienteRest", url = "http://54.164.31.75:8083/microservicio")
public interface  ClienteRest {

    @PostMapping
	String create(@RequestBody Map<String, String> body);

	@GetMapping("/{id}")
	String read(@PathVariable("id") String id);

	@PutMapping
	String update(@RequestParam("status") String status);

	@DeleteMapping
	String delete(@RequestHeader("Authorization") String authHeader);

}
