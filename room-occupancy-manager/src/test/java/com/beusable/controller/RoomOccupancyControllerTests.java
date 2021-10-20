package com.beusable.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(RoomOccupancyController.class)
public class RoomOccupancyControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testGetNumberOfOccupiedRoomsAndMoney() throws Exception {

		mockMvc.perform(get("/api/rooms/{freePremiumRooms}/{freeEconomyRooms}", 3, 3))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(get("/api/rooms/{freePremiumRooms}/{freeEconomyRooms}", 7, 5))
				.andExpect(status().isOk())
				.andReturn();
		
		mockMvc.perform(get("/api/rooms/{freePremiumRooms}/{freeEconomyRooms}", 2, 7))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testGetNumberOfOccupiedRoomsAndMoneyWithUpgradeGuests() throws Exception {

		mockMvc.perform(get("/api/rooms/{freePremiumRooms}/{freeEconomyRooms}", 7, 1))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(get("/api/rooms/{freePremiumRooms}/{freeEconomyRooms}", 8, 1))
				.andExpect(status().isOk())
				.andReturn();
	}

}
