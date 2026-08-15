package com.int2.email;

import com.int2.candidate.CandidateProfile;
import com.int2.company.Company;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class LocalEmailGenerator {
    private static final Map<String, String> SKILLS = new LinkedHashMap<>();
    static {
        SKILLS.put("python", "Python"); SKILLS.put("java", "Java"); SKILLS.put("javascript", "JavaScript");
        SKILLS.put("network", "network security"); SKILLS.put("penetration", "penetration testing");
        SKILLS.put("incident response", "incident response"); SKILLS.put("cloud", "cloud security");
        SKILLS.put("linux", "Linux"); SKILLS.put("soc", "SOC operations"); SKILLS.put("forensic", "digital forensics");
    }

    EmailResponse generate(CandidateProfile candidate, Company company) {
        String skills = detectedSkills(candidate.getCvText());
        String name = StringUtils.hasText(candidate.getName()) ? candidate.getName().trim() : "[Your Name]";
        String subject = "Cybersecurity Internship Inquiry - " + company.getName();
        String body = "Hello " + company.getName() + " Hiring Team,\n\n"
                + "I am writing to express my interest in a cybersecurity internship with your organization. "
                + "I am particularly interested in the opportunity to learn from and contribute to a NACSA-licensed cybersecurity provider.\n\n"
                + "My background includes " + skills + ". I am eager to apply these skills in a practical environment, support your team, and continue developing under experienced cybersecurity professionals.\n\n"
                + "I would appreciate the opportunity to discuss any current or upcoming internship openings. My CV is available for your consideration.\n\n"
                + "Thank you for your time.\n\nKind regards,\n" + name;
        return new EmailResponse(subject, body);
    }

    private String detectedSkills(String cvText) {
        String lower = cvText == null ? "" : cvText.toLowerCase(Locale.ROOT);
        List<String> found = SKILLS.entrySet().stream().filter(entry -> lower.contains(entry.getKey()))
                .map(Map.Entry::getValue).distinct().limit(3).toList();
        return found.isEmpty() ? "a strong interest in cybersecurity and a commitment to continuous learning"
                : String.join(", ", found);
    }
}
