package com.example.ezlawbackend.LawyerAuth.controller;

import com.example.ezlawbackend.LawyerAuth.dto.LawyerSignuprequest;
import com.example.ezlawbackend.LawyerAuth.model.Lawyer;
import com.example.ezlawbackend.LawyerAuth.service.LawyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/lawyerauth")
@CrossOrigin(origins = "http://localhost:3000",
        allowedHeaders = {"Content-Type", "Accept"},
        methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.DELETE, RequestMethod.PUT},
        allowCredentials = "true")
public class LawyerAuthController {

    @Autowired
    private LawyerService lawyerService;

    private static final String LAWYER_ID_KEY = "lawyerId";
    private static final String EMAIL_KEY = "email";
    private static final String ROLE_KEY = "role";

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody LawyerSignuprequest request) {
        Lawyer newLawyer = lawyerService.register(
                request.getFirstname(),
                request.getLastname(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                request.getGender(),
                request.getAddress(),
                request.getBio()
        );
        return ResponseEntity.ok("User registered successfully with ID: " + newLawyer.getLawyerid());
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginLawyer(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            if (session.getAttribute(EMAIL_KEY) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Already logged in"));
            }
            Lawyer lawyer = lawyerService.login(email, password);

            // Set session attributes
            session.setAttribute(LAWYER_ID_KEY, lawyer.getLawyerid());
            session.setAttribute(EMAIL_KEY, lawyer.getLawyerEmail());
            session.setAttribute(ROLE_KEY, lawyer.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("id", lawyer.getLawyerid());
            response.put("email", lawyer.getLawyerEmail());
            response.put("firstname", lawyer.getLawyerFirstname());
            response.put("lastname", lawyer.getLawyerLastname());
            response.put("role", lawyer.getRole());
            response.put("phone", lawyer.getPhone());
            response.put("gender", lawyer.getGender());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        String email = (String) session.getAttribute(EMAIL_KEY);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No active session"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("lawyerId", session.getAttribute(LAWYER_ID_KEY));
        response.put("email", email);
        response.put("role", session.getAttribute(ROLE_KEY));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit-profile")
    public ResponseEntity<?> editProfile(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String email = (String) session.getAttribute(EMAIL_KEY);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No active session"));
            }
            Lawyer updatedLawyer = lawyerService.updateProfile(
                    email,
                    request.get("firstname"),
                    request.get("lastname"),
                    request.get("phone"),
                    request.get("gender"),
                    request.get("address"),
                    request.get("bio")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedLawyer.getLawyerid());
            response.put("firstname", updatedLawyer.getLawyerFirstname());
            response.put("lastname", updatedLawyer.getLawyerLastname());
            response.put("phone", updatedLawyer.getPhone());
            response.put("gender", updatedLawyer.getGender());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> lawyerProfile(HttpSession session) {
        String email = (String) session.getAttribute(EMAIL_KEY);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No active session"));
        }

        try {
            Lawyer lawyer = lawyerService.getLawyerProfile(email);
            return ResponseEntity.ok(lawyer);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/lawyer-role")
    public ResponseEntity<?> checkRole(HttpSession session) {
        String email = (String) session.getAttribute(EMAIL_KEY);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No active session"));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("role", session.getAttribute(ROLE_KEY));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getlawyer")
    public ResponseEntity<?> getAllLawyers() {
        try {
            List<Lawyer> lawyers = lawyerService.getAllLawyer();
            return ResponseEntity.ok(lawyers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to fetch lawyers", "details", e.getMessage()));
        }
    }

    @GetMapping("/getlawyer-by-email/{email}")
    public ResponseEntity<?> getLawyerByEmail(@PathVariable String email) {
        try {
            Lawyer lawyer = lawyerService.findByEmail(email);
            if (lawyer == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Lawyer not found with email: " + email));
            }
            return ResponseEntity.ok(lawyer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


}
