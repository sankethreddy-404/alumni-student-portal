package com.alumniportal.service;

import com.alumniportal.entity.AlumniProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileScoreService {

    // The 7 fields that determine profile completeness, per spec:
    // Company, Skills, Location, Domain, Experience, Bio, LinkedIn
    public int calculateCompleteness(AlumniProfile profile) {
        int total = 7;
        int filled = 0;

        if (isFilled(profile.getCompany())) filled++;
        if (isFilled(profile.getSkills())) filled++;
        if (isFilled(profile.getLocation())) filled++;
        if (isFilled(profile.getDomain())) filled++;
        if (profile.getExperience() != null && profile.getExperience() >= 0) filled++;
        if (isFilled(profile.getBio())) filled++;
        if (isFilled(profile.getLinkedinUrl())) filled++;

        return Math.round((filled * 100.0f) / total);
    }

    public List<String> getMissingFields(AlumniProfile profile) {
        List<String> missing = new ArrayList<>();
        if (!isFilled(profile.getCompany())) missing.add("company");
        if (!isFilled(profile.getSkills())) missing.add("skills");
        if (!isFilled(profile.getLocation())) missing.add("location");
        if (!isFilled(profile.getDomain())) missing.add("domain");
        if (profile.getExperience() == null) missing.add("experience");
        if (!isFilled(profile.getBio())) missing.add("bio");
        if (!isFilled(profile.getLinkedinUrl())) missing.add("linkedinUrl");
        return missing;
    }

    private boolean isFilled(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
