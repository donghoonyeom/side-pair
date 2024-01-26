package sidepair.member.domain;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum SkillType {
    JAVA("Java"),
    PYTHON("Python"),
    JAVASCRIPT("JavaScript"),
    HTML("HTML"),
    CSS("CSS"),
    REACT("React"),
    ANGULAR("Angular"),
    DJANGO("Django"),
    SPRING_BOOT("Spring Boot"),
    FLASK("Flask"),
    SPRING("Spring"),
    NODEJS("Node.js"),
    DATABASE("Database"),
    GIT("Git"),
    UX_UI_DESIGN("UX/UI Design");

    private final String displayName;

    SkillType(String inputSkill) {
        validateNull(inputSkill);
        validateBlank(inputSkill);
        this.displayName = inputSkill.trim();
       // validateExistence(this.displayName);
    }

    private void validateNull(String displayName) {
        if (displayName == null) {
            throw new NullPointerException("기술 이름은 null일 수 없습니다.");
        }
    }

    private void validateBlank(String displayName) {
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("기술 이름은 공백일 수 없습니다.");
        }
    }

//    private void validateExistence(String displayName) {
//        if (Arrays.stream(values())
//                .noneMatch(skill -> skill.displayName.equals(displayName))) {
//            throw new IllegalArgumentException("존재하지 않는 기술입니다.");
//        }
//    }
}
