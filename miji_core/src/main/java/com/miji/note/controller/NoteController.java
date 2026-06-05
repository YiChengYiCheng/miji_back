package com.miji.note.controller;

import com.common.QO.note.AddNoteQO;
import com.common.QO.note.DeleteNoteQO;
import com.common.QO.note.NoteDetailQO;
import com.common.QO.note.NoteListQO;
import com.common.QO.note.UpdateNoteQO;
import com.common.result.Result;
import com.miji.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping("/add")
    public Result add(@RequestBody @Valid AddNoteQO qo) {
        return noteService.add(qo);
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody @Valid DeleteNoteQO qo) {
        return noteService.delete(qo);
    }

    @PostMapping("/update")
    public Result update(@RequestBody @Valid UpdateNoteQO qo) {
        return noteService.update(qo);
    }

    @PostMapping("/detail")
    public Result detail(@RequestBody @Valid NoteDetailQO qo) {
        return noteService.detail(qo);
    }

    @PostMapping("/list")
    public Result list(@RequestBody(required = false) NoteListQO qo) {
        return noteService.list(qo);
    }
}
