package com.kernelLetter.controller.letter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernelLetter.dto.LetterPatchDto;
import com.kernelLetter.dto.LetterSendDto;
import com.kernelLetter.global.error.ErrorCode;
import com.kernelLetter.global.error.exception.BusinessException;
import com.kernelLetter.service.LetterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LetterController.class)
@DisplayName("LetterController 단위 테스트")
class LetterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LetterService letterService;

    private LetterSendDto letterSendDto;
    private LetterPatchDto letterPatchDto;

    @BeforeEach
    void setUp() {
        letterSendDto = LetterSendDto.builder()
                .senderId(1L)
                .receiverId(2L)
                .content("테스트 편지 내용입니다.")
                .build();

        letterPatchDto = LetterPatchDto.builder()
                .senderId(1L)
                .content("수정된 편지 내용입니다.")
                .build();
    }

    @Test
    @DisplayName("POST /Letter - 정상적으로 편지를 전송한다")
    @WithMockUser
    void sendLetter_Success() throws Exception {
        // given
        doNothing().when(letterService).sendLetter(any(LetterSendDto.class));

        // when & then
        mockMvc.perform(post("/Letter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(letterSendDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(letterService, times(1)).sendLetter(any(LetterSendDto.class));
    }

    @Test
    @DisplayName("POST /Letter - 존재하지 않는 사용자로 편지 전송 시 예외가 발생한다")
    @WithMockUser
    void sendLetter_UserNotFound_ReturnsError() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.USER_NOT_EXISTS))
                .when(letterService).sendLetter(any(LetterSendDto.class));

        // when & then
        mockMvc.perform(post("/Letter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(letterSendDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(letterService, times(1)).sendLetter(any(LetterSendDto.class));
    }

    @Test
    @DisplayName("POST /Letter - 잘못된 JSON 형식으로 요청 시 400 에러가 발생한다")
    @WithMockUser
    void sendLetter_InvalidJson_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/Letter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(letterService, never()).sendLetter(any(LetterSendDto.class));
    }

    @Test
    @DisplayName("POST /Letter - Content-Type이 없는 요청 시 415 에러가 발생한다")
    @WithMockUser
    void sendLetter_NoContentType_ReturnsUnsupportedMediaType() throws Exception {
        // when & then
        mockMvc.perform(post("/Letter")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(letterSendDto)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("POST /Letter - null 값이 포함된 DTO로 요청한다")
    @WithMockUser
    void sendLetter_NullValues_Success() throws Exception {
        // given
        LetterSendDto nullDto = LetterSendDto.builder()
                .senderId(null)
                .receiverId(null)
                .content(null)
                .build();

        doNothing().when(letterService).sendLetter(any(LetterSendDto.class));

        // when & then
        mockMvc.perform(post("/Letter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullDto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /Letter - 빈 내용의 편지를 전송한다")
    @WithMockUser
    void sendLetter_EmptyContent_Success() throws Exception {
        // given
        LetterSendDto emptyContentDto = LetterSendDto.builder()
                .senderId(1L)
                .receiverId(2L)
                .content("")
                .build();

        doNothing().when(letterService).sendLetter(any(LetterSendDto.class));

        // when & then
        mockMvc.perform(post("/Letter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyContentDto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /Letter/{receiverId} - 정상적으로 편지를 수정한다")
    @WithMockUser
    void update_Success() throws Exception {
        // given
        Long receiverId = 2L;
        doNothing().when(letterService).patch(eq(receiverId), any(LetterPatchDto.class));

        // when & then
        mockMvc.perform(patch("/Letter/{receiverId}", receiverId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(letterPatchDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("수정이 완료되었습니다."));

        verify(letterService, times(1)).patch(eq(receiverId), any(LetterPatchDto.class));
    }

    @Test
    @DisplayName("PATCH /Letter/{receiverId} - 존재하지 않는 편지 수정 시 예외가 발생한다")
    @WithMockUser
    void update_LetterNotFound_ReturnsError() throws Exception {
        // given
        Long receiverId = 2L;
        doThrow(new BusinessException(ErrorCode.LETTER_NOT_EXISTS))
                .when(letterService).patch(eq(receiverId), any(LetterPatchDto.class));

        // when & then
        mockMvc.perform(patch("/Letter/{receiverId}", receiverId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(letterPatchDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(letterService, times(1)).patch(eq(receiverId), any(LetterPatchDto.class));
    }

    @Test
    @DisplayName("PATCH /Letter/{receiverId} - 잘못된 receiverId 타입으로 요청 시 400 에러가 발생한다")
    @WithMockUser
    void update_InvalidReceiverIdType_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(patch("/Letter/{receiverId}", "invalid")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(letterPatchDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(letterService, never()).patch(any(), any(LetterPatchDto.class));
    }

    @Test
    @DisplayName("PATCH /Letter/{receiverId} - 음수 receiverId로 요청한다")
    @WithMockUser
    void update_NegativeReceiverId_CallsService() throws Exception {
        // given
        Long negativeReceiverId = -1L;
        doThrow(new BusinessException(ErrorCode.LETTER_NOT_EXISTS))
                .when(letterService).patch(eq(negativeReceiverId), any(LetterPatchDto.class));

        // when & then
        mockMvc.perform(patch("/Letter/{receiverId}", negativeReceiverId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(letterPatchDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(letterService, times(1)).patch(eq(negativeReceiverId), any(LetterPatchDto.class));
    }

    @Test
    @DisplayName("PATCH /Letter/{receiverId} - 빈 내용으로 편지를 수정한다")
    @WithMockUser
    void update_EmptyContent_Success() throws Exception {
        // given
        Long receiverId = 2L;
        LetterPatchDto emptyContentDto = LetterPatchDto.builder()
                .senderId(1L)
                .content("")
                .build();

        doNothing().when(letterService).patch(eq(receiverId), any(LetterPatchDto.class));

        // when & then
        mockMvc.perform(patch("/Letter/{receiverId}", receiverId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyContentDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("수정이 완료되었습니다."));
    }

    @Test
    @DisplayName("PATCH /Letter/{receiverId} - 특수 문자가 포함된 내용으로 수정한다")
    @WithMockUser
    void update_SpecialCharacters_Success() throws Exception {
        // given
        Long receiverId = 2L;
        LetterPatchDto specialCharDto = LetterPatchDto.builder()
                .senderId(1L)
                .content("특수문자: !@#$%^&*()_+-=[]{}|;':\",./<>?")
                .build();

        doNothing().when(letterService).patch(eq(receiverId), any(LetterPatchDto.class));

        // when & then
        mockMvc.perform(patch("/Letter/{receiverId}", receiverId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialCharDto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /Letter/{receiverId} - 매우 긴 내용으로 수정한다")
    @WithMockUser
    void update_LongContent_Success() throws Exception {
        // given
        Long receiverId = 2L;
        String longContent = "a".repeat(10000);
        LetterPatchDto longContentDto = LetterPatchDto.builder()
                .senderId(1L)
                .content(longContent)
                .build();

        doNothing().when(letterService).patch(eq(receiverId), any(LetterPatchDto.class));

        // when & then
        mockMvc.perform(patch("/Letter/{receiverId}", receiverId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longContentDto)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}