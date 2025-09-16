package com.babymate.forum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
@Entity
@Table(name = "boards")
public class BoardVO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private Integer boardId;
    private String boardName;
    private String boardLine;
    private Byte boardStatus;

    public BoardVO() { // JPA 需要無參建構子
    }

    public BoardVO(Integer boardId, String boardName, String boardLine) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.boardLine = boardLine;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    public Integer getBoardId() {
        return boardId;
    }

    public void setBoardId(Integer boardId) {
        this.boardId = boardId;
    }

    @Column(name = "board_title", length = 50, nullable = false)
    @NotEmpty(message = "看板名稱: 請勿空白")
    @Size(min = 2, max = 50, message = "看板名稱: 長度必須在{min}到{max}之間")
    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    @Column(name = "board_line", length = 50, nullable = false)
    @NotEmpty(message = "看板描述: 請勿空白")
    @Size(min = 2, max = 50, message = "看板描述: 長度必須在{min}到{max}之間")
    public String getBoardLine() {
        return boardLine;
    }

    public void setBoardLine(String boardLine) {
        this.boardLine = boardLine;
    }
    @Column(name = "board_status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    public Byte getBoardStatus() { return boardStatus; }
    public void setBoardStatus(Byte boardStatus) { this.boardStatus = boardStatus; }
}