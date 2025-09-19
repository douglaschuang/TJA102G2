//package com.babymate.forum.model;
//
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import com.babymate.member.model.MemberVO;
//
//public interface LikesRepository extends JpaRepository<LikesVO, Integer> {
//
//	boolean existsByMemberVO_MemberIdAndPostVO_PostId(Integer memberId, Integer postId);
//
//    Optional<LikesVO> findByMemberVOAndPostVO(MemberVO memberVO, PostVO postVO);
//
//}
