package com.example.ezlawbackend.LawyerAuth.service;

import com.example.ezlawbackend.LawyerAuth.model.Lawyer;
import com.example.ezlawbackend.LawyerAuth.repository.LawyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LawyerService {

    @Autowired
    private LawyerRepository lawyerRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Lawyer register(String firstname, String lastname, String email, String password, String phone, String gender, String info,String bio,String profileImageUrl) {
        if (lawyerRepository.findByLawyerEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);

        Lawyer lawyer = new Lawyer(firstname,lastname,email,hashedPassword,"Lawyer",phone,gender,info,bio,profileImageUrl);
        return lawyerRepository.save(lawyer);
    }

    public Lawyer login(String email, String password) {
        Lawyer lawyer = lawyerRepository.findByLawyerEmail(email);
        if (lawyer == null) {
            throw new RuntimeException("Lawyer not found");
        }
        if (!passwordEncoder.matches(password, lawyer.getHashedPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return lawyer;
    }


    public Lawyer updateProfile(String email, String firstname, String lastname, String phone, String gender,String address, String bio,String profileImageUrl) {
        Lawyer lawyer = lawyerRepository.findByLawyerEmail(email);
        if (lawyer == null) {
            throw new RuntimeException("Lawyer not found");
        }

        lawyer.setLawyerFirstname(firstname);
        lawyer.setLawyerLastname(lastname);
        lawyer.setPhone(phone);
        lawyer.setGender(gender);
        lawyer.setAddress(address);
        lawyer.setBio(bio);
        lawyer.setProfileImageUrl(profileImageUrl);

        return lawyerRepository.save(lawyer);
    }

    public Lawyer getLawyerProfile(String email) {
        Lawyer lawyer = lawyerRepository.findByLawyerEmail(email);
        if (lawyer == null) {
            throw new RuntimeException("Lawyer not found");
        }
        return lawyer;
    }

    public List<Lawyer> getAllLawyer(){
        return lawyerRepository.findAll();
    }

    public Lawyer findByEmail(String email) {
        return lawyerRepository.findByLawyerEmail(email);
    }

}