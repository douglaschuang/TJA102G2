//package com.babymate.forum.model;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.babymate.member.model.MemberRepository;
//import com.babymate.member.model.MemberVO;
//
//public class LikesService {
//	
//	@Autowired
//	private MemberRepository memberRepository;
//
//    @Autowired
//    private LikesRepository likesRepository;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    public LikesVO likePost(Integer memberId, Integer postId) {
//        // 先抓文章
//        PostVO post = postRepository.findById(postId)
//                      .orElseThrow(() -> new RuntimeException("文章不存在"));
//
//        // 先抓會員
//        MemberVO member = memberRepository.findById(memberId)
//                         .orElseThrow(() -> new RuntimeException("會員不存在"));
//
//        // 判斷是否已按過愛心
//        if (likesRepository.existsByMemberVO_MemberIdAndPostVO_PostId(memberId, postId)) {
//            throw new RuntimeException("已按過愛心");
//        }
//
//        // 建立 LikesVO
//        LikesVO like = new LikesVO();
//        like.setMemberVO(member);
//        like.setPostVO(post);
//
//        return likesRepository.save(like);
//    }
//
//    public void unlikePost(Integer memberId, Integer postId) {
//        // 先抓會員
//        MemberVO member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new RuntimeException("會員不存在"));
//
//        // 先抓文章
//        PostVO post = postRepository.findById(postId)
//                .orElseThrow(() -> new RuntimeException("文章不存在"));
//
//        // 找對應的 Like
//        LikesVO like = likesRepository.findByMemberVOAndPostVO(member, post).orElseThrow(() -> new RuntimeException("尚未按愛心"));
//
//        likesRepository.delete(like);
//    }
//}