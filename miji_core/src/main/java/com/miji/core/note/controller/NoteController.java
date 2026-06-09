package com.miji.core.note.controller;

import com.common.QO.note.AddNoteQO;
import com.common.QO.note.DeleteNoteQO;
import com.common.QO.note.NoteDetailQO;
import com.common.QO.note.NoteListQO;
import com.common.QO.note.UpdateNoteQO;
import com.common.result.Result;
import com.miji.core.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping("/add")
    public Result add(@RequestBody @Valid AddNoteQO qo, HttpServletRequest request) {
        return noteService.add(qo, getUserId(request));
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody @Valid DeleteNoteQO qo, HttpServletRequest request) {
        return noteService.delete(qo, getUserId(request));
    }

    @PostMapping("/update")
    public Result update(@RequestBody @Valid UpdateNoteQO qo, HttpServletRequest request) {
        return noteService.update(qo, getUserId(request));
    }

    @PostMapping("/detail")
    public Result detail(@RequestBody @Valid NoteDetailQO qo, HttpServletRequest request) {
        return noteService.detail(qo, getUserId(request));
    }

    @PostMapping("/list")
    public Result list(@RequestBody(required = false) NoteListQO qo, HttpServletRequest request) {
        return noteService.list(qo, getUserId(request));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
