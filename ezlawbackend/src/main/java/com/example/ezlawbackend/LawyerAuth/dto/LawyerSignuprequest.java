package com.example.ezlawbackend.LawyerAuth.dto;


public class LawyerSignuprequest {
    private String lawyerFirstname;
    private String lawyerLastname;
    private String lawyerEmail;
    private String password;
    private String phone;
    private String gender;
    private String address;
    private String bio;
    private String profileImageUrl;

    public String getlawyerFirstname() {
        return lawyerFirstname;
    }

    public void setlawyerFirstname(String lawyerFirstname) {
        this.lawyerFirstname = lawyerFirstname;
    }

    public String getlawyerLastname() {
        return lawyerLastname;
    }

    public void setlawyerLastname(String lawyerLastname) {
        this.lawyerLastname = lawyerLastname;
    }

    public String getlawyerEmail() {
        return lawyerEmail;
    }

    public void setlawyerEmail(String lawyerEmail) {
        this.lawyerEmail = lawyerEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getGender(){
        return gender;
    }

    public void setGender(String gender){
        this.gender = gender;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public String getBio(){
        return bio;
    }

    public void setBio(String bio){
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}