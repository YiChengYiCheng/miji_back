package com.miji.note.service;

import com.common.QO.note.AddNoteQO;
import com.common.QO.note.DeleteNoteQO;
import com.common.QO.note.NoteDetailQO;
import com.common.QO.note.NoteListQO;
import com.common.QO.note.UpdateNoteQO;
import com.common.result.Result;

import javax.validation.Valid;

public interface NoteService {
    Result add(@Valid AddNoteQO qo, Long currentUserId);

    Result delete(@Valid DeleteNoteQO qo, Long currentUserId);

    Result update(@Valid UpdateNoteQO qo, Long currentUserId);

    Result detail(@Valid NoteDetailQO qo);

    Result list(NoteListQO qo);
}
