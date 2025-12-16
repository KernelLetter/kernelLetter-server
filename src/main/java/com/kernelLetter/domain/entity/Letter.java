package com.kernelLetter.domain.entity;

import com.kernelLetter.dto.LetterSendDto;
import com.kernelLetter.dto.LetterPatchDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name= "letter")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Letter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "sender", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver", nullable = false)
    private User receiver;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column
    private int position;

    public static Letter from(User sender, User receiver, String content, int position) {
        return Letter.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .position(position)
                .build();
    }
    public void setContent(LetterPatchDto dto) {
        this.content = dto.getContent();
    }

}
