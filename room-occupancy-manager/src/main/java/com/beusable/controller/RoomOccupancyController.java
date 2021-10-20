package com.beusable.controller;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api")
public class RoomOccupancyController {

	final int ONE_HUNDRED = 100;
	
	// http://localhost:8080/api/rooms/7/1
	@GetMapping(value = "/rooms/{freePremiumRooms}/{freeEconomyRooms}")
	public ResponseEntity<String> getNumberOfOccupiedRoomsAndMoney( 
			@PathVariable("freePremiumRooms") Integer freePremiumRooms,
			@PathVariable("freeEconomyRooms") Integer freeEconomyRooms) throws Exception {
				
		List<Double> potentialGuests = getPotentialGuestsFromFile();
		Collections.sort(potentialGuests, Collections.reverseOrder());

		List<Double> premiumUsage = new ArrayList<>();
		List<Double> economyUsage = new ArrayList<>();
		Double premiumMoney = 0.0;
		Double economyMoney = 0.0;
		Integer highestPayingCustomerStartIndex = getHighestPayingCustomerStartIndex(potentialGuests);

		if (!potentialGuests.isEmpty()) {
			// if the guests had at least one guest pay less EUR 100
			if (potentialGuests.get(potentialGuests.size() - 1) < ONE_HUNDRED) {
				economyMoney = fillEconomyRooms(potentialGuests, freePremiumRooms, freeEconomyRooms, economyUsage,
						economyMoney, highestPayingCustomerStartIndex);
			}
			premiumMoney = fillPremiumRooms(potentialGuests, freePremiumRooms, freeEconomyRooms, premiumUsage,
					economyUsage, premiumMoney, highestPayingCustomerStartIndex);
		}
		String output = "Usage Premium: " + premiumUsage.size() + " (EUR " + premiumMoney + ")\n"+
						"Usage Economy: " + economyUsage.size() + " (EUR " + economyMoney + ")";
	
		logger.info("output={}",output);
		
		return (output.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(output));
	}

	private Double fillEconomyRooms(List<Double> potentialGuests, Integer freePremiumRooms,
			Integer freeEconomyRooms, List<Double> economyUsage, Double economyMoney,
			Integer highestPayingCustomerStartIndex) {
	 
		// LowerPayingCustomer numbers < freeEconomyRooms so no upgrade will happen for any guest pay less EUR 100
		// Or the freePremiumRooms < guests are paying >=s EUR 100
		if ((potentialGuests.size() - highestPayingCustomerStartIndex) <= freeEconomyRooms
				|| freePremiumRooms < highestPayingCustomerStartIndex) {

			economyMoney = fillEconomyRoomsStartFrom(potentialGuests, freeEconomyRooms, economyUsage,
					economyMoney, highestPayingCustomerStartIndex);

		} else if ((freePremiumRooms + freeEconomyRooms) > potentialGuests.size()) { // if the available rooms more than guests and all guests pay less EUR 100

			for (int i = potentialGuests.size() - freeEconomyRooms; i < potentialGuests.size(); i++) {
				economyUsage.add(potentialGuests.get(i));
				economyMoney += potentialGuests.get(i);
			}

		} else {// LowerPayingCustomer numbers > freeEconomyRooms so upgrade will happen for highestPayingCustomer pay less EUR 100
			economyMoney = fillEconomyRoomsStartFrom(potentialGuests, freeEconomyRooms, economyUsage,
					economyMoney, freePremiumRooms);
		}
		return economyMoney;
	}

	private Double fillEconomyRoomsStartFrom(List<Double> potentialGuests, Integer freeEconomyRooms,
			List<Double> economyUsage, Double economyMoney, Integer startIndex) {
		
		for (int i = startIndex; (i < potentialGuests.size()  && economyUsage.size() < freeEconomyRooms) ; i++) {
			economyUsage.add(potentialGuests.get(i));
			economyMoney += potentialGuests.get(i);
		}
		return economyMoney;
	}

	private Double fillPremiumRooms(List<Double> potentialGuests, Integer freePremiumRooms,
			Integer freeEconomyRooms, List<Double> premiumUsage, List<Double> economyUsage, Double premiumMoney,
			Integer highestPayingCustomerStartIndex) {
		// All guests pay >= EUR 100
		// OR freePremiumRooms <= #guests pay >= EUR 100
		// OR available rooms < # guests
		if (potentialGuests.get(potentialGuests.size() - 1) >= ONE_HUNDRED
				|| freePremiumRooms <= highestPayingCustomerStartIndex
				|| (freePremiumRooms + freeEconomyRooms) <= potentialGuests.size()) {
			premiumMoney = fillPremiumRoomsWithPremiumGuestsOnly(potentialGuests, freePremiumRooms, premiumUsage,
					premiumMoney);
		} else {
			for (int i = 0; (i < potentialGuests.size() - economyUsage.size()); i++) {
				premiumUsage.add(potentialGuests.get(i));
				premiumMoney += potentialGuests.get(i);
			}
		} 
		
		return premiumMoney;
	}

	// All the PremiumRooms occupied with Premium Guests only who pay >= 100
	private Double fillPremiumRoomsWithPremiumGuestsOnly(List<Double> potentialGuests, Integer freePremiumRooms,
			List<Double> premiumUsage, Double premiumMoney) {
		
		for (int i = 0; i < freePremiumRooms && i < potentialGuests.size(); i++) {
			premiumUsage.add(potentialGuests.get(i));
			premiumMoney += potentialGuests.get(i);
		}
		return premiumMoney;
	}

	private Integer getHighestPayingCustomerStartIndex(List<Double> potentialGuests) {
		
		for (int i = 0; i < potentialGuests.size(); i++) {
			if (potentialGuests.get(i) < ONE_HUNDRED) {
				return i;
			}
		}
		return 0;
	}

	private List<Double> getPotentialGuestsFromFile() throws Exception{
		
		List<Object> potentialGuests = new ArrayList<>();
		JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader("src/main/resources/PotentialGuests.json")){
        	JSONObject  jsonObject = (JSONObject)jsonParser.parse(reader);
        	JSONArray jsonArray = (JSONArray) jsonObject.get("guests");
            potentialGuests = Arrays.asList(jsonArray.toArray());
        } catch (IOException | ParseException e) {
        	throw new Exception("Error occurred while reading the file",e);
        }
	    
	    return convertList(potentialGuests, obj -> Double.parseDouble(obj.toString()) );
	}
		
	private <T, U> List<U> convertList(List<T> from, Function<T, U> func) {
		
	    return from.stream().map(func).collect(Collectors.toList());
	}
	
	Logger logger = LoggerFactory.getLogger(RoomOccupancyController.class);
	
}
