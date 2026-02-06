package com.stgsporting.piehmecup.autoload;

import com.stgsporting.piehmecup.dtos.admins.AdminFormDTO;
import com.stgsporting.piehmecup.entities.Price;
import com.stgsporting.piehmecup.entities.SchoolYear;
import com.stgsporting.piehmecup.enums.Role;
import com.stgsporting.piehmecup.repositories.AdminRepository;
import com.stgsporting.piehmecup.repositories.PriceRepository;
import com.stgsporting.piehmecup.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class AdminLoader implements CommandLineRunner {

    private final AdminService adminService;

    AdminLoader(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) throws Exception {
        if(adminService.getAllAdmins().isEmpty()) {
            AdminFormDTO admin = new AdminFormDTO();

            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setRole(Role.ADMIN.name());
            admin.setSchoolYear(1L);

            adminService.createAdmin(admin);
        }
    }
}
