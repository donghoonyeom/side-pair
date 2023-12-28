package sidepair.member.domain.vo;

import lombok.Getter;

@Getter
public enum Skill {
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

    Skill(String displayName) {
        this.displayName = displayName;
    }


}
