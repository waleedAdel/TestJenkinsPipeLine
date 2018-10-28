package com.example.easynotes.controller;

import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.Note;
import com.example.easynotes.repository.NoteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rajeevkumarsingh on 27/06/17.
 */
@RestController
@RequestMapping("/api")
public class NoteController {

  private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    NoteRepository noteRepository;

    @GetMapping("/notes")
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    @PostMapping("/notes")
    public Note createNote(@Valid @RequestBody Note note) {

        String request = "{\"amount\":{\"value\":5000,\"currency\":\"SAR\"},\"merchantAccount\":\"CareemKSA\",\"recvcrence\":\"26790397_TOP_UP_50_1589722_2018-10-20T11:0\",\"shopperEmail\":\"sasitc10100@hotmail.com\",\"shopperReference\":\"USER_024f5f5da5b03387dc73ed3f4ecf966777cf3bfb\",\"selectedRecurringDetailReference\":\"2915397862112498\",\"shopperInteraction\":\"Ecommerce\",\"recurring\":{\"contract\":\"ONECLICK\"},\"browserInfo\":{\"acceptHeader\":\"application/json, application/json, application/*+json, application/*+json\",\"userAgent\":\"ACMA/8.7.0\"},\"captureDelayHours\":0,\"card\":{\"cvc\":\"220\"}}";
        System.out.println(censorCreditCardTransactionRequest(request));

        return noteRepository.save(note);
    }

    private String censorCreditCardTransactionRequest(String request) {
        request = censorMessage(request, "cvc");
        return censorMessage(request, "cvv");
    }

    public static String objectToJsonString(Object o){
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
        }
        return "";
    }

    @GetMapping("/notes/{id}")
    public Note getNoteById(@PathVariable(value = "id") Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));
    }

    @PutMapping("/notes/{id}")
    public Note updateNote(@PathVariable(value = "id") Long noteId,
                                           @Valid @RequestBody Note noteDetails) {

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());

        Note updatedNote = noteRepository.save(note);
        return updatedNote;
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable(value = "id") Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        noteRepository.delete(note);

        return ResponseEntity.ok().build();
    }


    public static String censorMessage(String message) {

        return message;
    }

    public static String censorMessage(String message, String key) {
        if (uriContains(message, key)) {
            String searchRegex = "\"" + key + "\"\\s*:\\s*\".*?\"";
            String replaceString = String.format("\"" + key + "\":\"%s\"", "**censored**");
            message = censor(message, searchRegex, replaceString);
        }

        return message;
    }


    private static String censor(String message, String searchRegex, String replaceString) {
        Pattern regexPattern = Pattern.compile(searchRegex);
        Matcher m = regexPattern.matcher(message);
        message = m.replaceAll(replaceString);
        return message;
    }


    private static boolean uriContains(String uri, String searchToken) {
        return StringUtils.isNotBlank(uri) && uri.toLowerCase().contains(searchToken.toLowerCase());
    }

    private static boolean uriContains(String uri, AbstractList<String> searchTokens) {
        if (StringUtils.isBlank(uri)) {
            return false;
        } else {
            Iterator var2 = searchTokens.iterator();

            String searchToken;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                searchToken = (String)var2.next();
            } while(!uriContains(uri, searchToken));

            return true;
        }
    }


}
