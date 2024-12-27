package com.example.foodorder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

	User findByRegistrationNumber(String registrationNumber);
	 User findByEmail(String email);
	 java.util.List<User> findAll();
}
