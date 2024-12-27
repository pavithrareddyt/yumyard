package com.example.foodorder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;


@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;
    


    @Autowired
    private HttpSession session;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/menu")
    public String menu() {
        return "menu";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String showsignupPage() {
        return "signup";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/yummy")
    public String yummy() {
        return "yummy";
    }

    @GetMapping("/annapurna")
    public String annapurna() {
        return "annapurna";
    }

    @GetMapping("/dominos")
    public String dominos() {
        return "dominos";
    }

    @GetMapping("/kfc")
    public String kfc() {
        return "kfc";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }
    
    @GetMapping("/payment")
    public String payment() {
        return "payment";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "orders";
    }

    @PostMapping("/up")
    public String signup(@RequestParam String email,
                         @RequestParam String registrationNumber,
                         @RequestParam String password,
                         HttpSession session, Model model) {
        User existingUser = userRepository.findByRegistrationNumber(registrationNumber);
        if (existingUser != null) {
            model.addAttribute("message", "You have already signed up with this registration number.");
            return "signup";
        }
        User existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail != null) {
            model.addAttribute("message", "You have already signed up with this email.");
            return "signup";
        }
        
        
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setRegistrationNumber(registrationNumber);
        newUser.setPassword(password);
        userRepository.save(newUser);
        session.setAttribute("currentUser", newUser);
        
       
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String registrationNumber,
                        @RequestParam String password, Model model) {
        User user = userRepository.findByRegistrationNumber(registrationNumber);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("currentUser", user);
            return "menu";
        } else {
            model.addAttribute("errorMessage", "Invalid credentials!");
            return "login";
        }
    }

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";

    @PostMapping("/admin")
    public String handleAdminLogin(@RequestParam String username,
                                   @RequestParam String password,
                                   Model model) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            List<User> users = userRepository.findAll();
            model.addAttribute("users", users);
            return "orders";
        } else {
            model.addAttribute("errorMessage", "Invalid admin credentials!");
            return "admin";
        }
    }

    @PostMapping("/deleteOrder")
    public String deleteOrder(@RequestParam String registrationNumber, Model model) {
        User userToDelete = userRepository.findByRegistrationNumber(registrationNumber);
        if (userToDelete != null) {
            userRepository.delete(userToDelete);
        }
        List<User> remainingUsers = userRepository.findAll();
        model.addAttribute("users", remainingUsers);
        return "redirect:/orders";
    }

    @PostMapping("/saveRestaurant")
    public String saveRestaurant(@RequestParam String restaurantName, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            model.addAttribute("errorMessage", "No user is logged in.");
            return "login";
        }
        currentUser.setRestaurantName(restaurantName);
        userRepository.save(currentUser);
        model.addAttribute("message", "Restaurant " + restaurantName + " saved successfully!");
        return "redirect:/" + restaurantName;
    }
    
    @PostMapping("/submitPayment")
    public String processPayment(@RequestParam(value = "paymentMethod", required = false) String paymentMethod, Model model) {
        // Validate the payment method
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            model.addAttribute("error", "Please select a payment method.");
            return "payment"; // Return the same page with an error message
        }

        // Process payment method
        if (paymentMethod.equals("online")) {
            model.addAttribute("message", "You selected Online Payment.");
        } else if (paymentMethod.equals("offline")) {
            model.addAttribute("message", "You selected Cash on Delivery (COD).");
        }

        // Redirect to the success page
        return "success"; // Ensure success.html exists in the templates folder
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam("itemName") String[] itemNames,
                           @RequestParam("itemQuantity") String[] itemQuantities,
                           @RequestParam("totalCost") String totalCost,
                           Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("No user is logged in");
        }
        try {
            currentUser.setTotalCost(Double.parseDouble(totalCost));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid total cost format");
        }
        StringBuilder items = new StringBuilder();
        int totalQuantity = 0;
        for (int i = 0; i < itemNames.length; i++) {
            items.append(itemNames[i]).append(" x ").append(itemQuantities[i]).append(", ");
            totalQuantity += Integer.parseInt(itemQuantities[i]);
        }
        currentUser.setItemName(items.toString());
        currentUser.setItemQuantity(totalQuantity);
        currentUser.setTotalCost(Double.parseDouble(totalCost));
        userRepository.save(currentUser);
        model.addAttribute("totalCost", totalCost);
        return "payment";
    }

    public User getCurrentUser() {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            return userRepository.findByRegistrationNumber(currentUser.getRegistrationNumber());
        } else {
            throw new RuntimeException("No user is currently logged in");
        }
    }
}
