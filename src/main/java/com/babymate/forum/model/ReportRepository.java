package com.babymate.forum.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.babymate.member.model.MemberVO;

public interface ReportRepository  extends JpaRepository<ReportVO, Integer> {

    /**
     * ★★★ Spring Data JPA 的黑魔法 ★★★
     * 檢查指定的會員(memberVO)是否已經對指定的文章(postVO)提交過檢舉。
     * 你不需要寫任何 SQL，只要按照命名慣例，Spring 就會自動幫你實現這個功能。
     * * @param postVO 被檢舉的文章實體
     * @param memberVO 檢舉的會員實體
     * @return 如果存在，返回 true；否則返回 false
     */
    boolean existsByPostVOAndMemberVO(PostVO postVO, MemberVO memberVO);

//    @Query("SELECT r FROM ReportVO r " +
//            "JOIN FETCH r.postVO p " +
//            "JOIN FETCH p.boardVO " +
//            "JOIN FETCH r.memberVO " +
//            "ORDER BY r.reportTime DESC")
//     Page<ReportVO> findAllWithDetails(Pageable pageable);
    


        /**
         * ★★★ 王者之道：使用 @EntityGraph ★★★
         * 我們覆寫 JpaRepository 預設的 findAll 方法。
         * @EntityGraph 這個註解，就是在告訴 JPA：
         * 「當你執行這個 findAll 方法時，務必、一定、立刻把 postVO 和 memberVO 
         * 這兩個屬性，以及 postVO 裡面的 boardVO，全部都抓取回來！」
         *
         * 這樣，就算 Service 呼叫的是預設的 findAll，也能達到 JOIN FETCH 的效果。
         */
        @Override
        @EntityGraph(attributePaths = { "postVO", "postVO.boardVO", "memberVO" })
        Page<ReportVO> findAll(Pageable pageable);

    }


