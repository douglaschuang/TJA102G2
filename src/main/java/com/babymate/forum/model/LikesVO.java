//package com.babymate.forum.model;
//
//import java.sql.Timestamp;
//
//import com.babymate.member.model.MemberVO;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//
//public class LikesVO  implements java.io.Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    private Integer likeId;
//    private PostVO postVO;        // 對應文章
//    private MemberVO memberVO;    // 對應會員
//    private Timestamp likeTime;   // 對應資料表 like_time
//
//    public LikesVO() {}
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "like_id")
//    public Integer getLikeId() {
//        return likeId;
//    }
//
//    public void setLikeId(Integer likeId) {
//        this.likeId = likeId;
//    }
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id", nullable = false)
//    public PostVO getPostVO() {
//        return postVO;
//    }
//
//    public void setPostVO(PostVO postVO) {
//        this.postVO = postVO;
//    }
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", nullable = false)
//    public MemberVO getMemberVO() {
//        return memberVO;
//    }
//
//    public void setMemberVO(MemberVO memberVO) {
//        this.memberVO = memberVO;
//    }
//
//    @Column(name = "like_time", insertable = false, updatable = false,
//            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
//    public Timestamp getLikeTime() {
//        return likeTime;
//    }
//
//    public void setLikeTime(Timestamp likeTime) {
//        this.likeTime = likeTime;
//    }
//}
