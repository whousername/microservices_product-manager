package com.lopatin.inventory_service.inventory_service;

import com.lopatin.inventory_service.inventory_service.model.Inventory;
import com.lopatin.inventory_service.inventory_service.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}


    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository){
        return args -> {

            Inventory inventory = new Inventory();
            inventory.setSkuCode("iphone_17");
            inventory.setQuantity(100);

            Inventory inventory1 = new Inventory();
            inventory1.setSkuCode("iphone_14");
            inventory1.setQuantity(0);

            inventoryRepository.saveAll(List.of(inventory, inventory1));
        };
    }


}
