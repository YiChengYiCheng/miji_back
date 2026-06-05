package com.miji.note.service;

import com.common.QO.note.AddNoteQO;
import com.common.QO.note.DeleteNoteQO;
import com.common.QO.note.NoteDetailQO;
import com.common.QO.note.NoteListQO;
import com.common.QO.note.UpdateNoteQO;
import com.common.result.Result;

import javax.validation.Valid;

public interface NoteService {
    Result add(@Valid AddNoteQO qo);

    Result delete(@Valid DeleteNoteQO qo);

    Result update(@Valid UpdateNoteQO qo);

    Result detail(@Valid NoteDetailQO qo);

    Result list(NoteListQO qo);
}
