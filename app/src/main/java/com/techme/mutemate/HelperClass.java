package com.techme.mutemate;

public class HelperClass {

    String name,username,email,birthdate, gender, country, streak,xp, completion_point, contact;

    public HelperClass() {
    }

    public HelperClass(String name, String username, String email,String streak,String xp) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.streak = streak;
        this.xp = xp;
    }
    public HelperClass( String username, String email,String birthdate, String gender, String country, String contact ) {

        this.username = username;
        this.email = email;
        this.birthdate = birthdate;
        this.gender = gender;
        this.country = country;
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getStreak() {
        return streak;
    }

    public void setStreak(String streak) {
        this.streak = streak;
    }

    public String getXp() {
        return xp;
    }

    public void setXp(String xp) {
        this.xp = xp;
    }

    public String getName() {
        return name;
    }

    public String getCompletion_point() {
        return completion_point;
    }

    public void setCompletion_point(String completion_point) {
        this.completion_point = completion_point;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
