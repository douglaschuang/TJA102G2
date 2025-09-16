package com.babymate.forum.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    // 取得所有看板
    public List<BoardVO> findAllBoards() {
        return boardRepository.findAll();
    }

    // 透過 ID 取得單一看板
    public Optional<BoardVO> findBoardById(Integer boardId) {
        return boardRepository.findById(boardId);
    }

    // 新增或更新看板
//    public BoardVO saveBoard(BoardVO board) {
//        return boardRepository.save(board);
//    }

    // 刪除看板
//    public void deleteBoard(Integer boardId) {
//        boardRepository.deleteById(boardId);
//    }

    // 如果你想加其他條件查詢，也可以在這裡擴展方法
    // 例如依名稱查詢
//    public List<BoardVO> findByBoardName(String name) {
//        return boardRepository.findByBoardName(name);
//    }
}
