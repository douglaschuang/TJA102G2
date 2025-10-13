package com.babymate.member.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.babymate.member.model.MemberVO;
import com.babymate.util.EncodingUtil;
import com.babymate.member.model.MemberRepository;

/**
 * 會員服務層（Service）
 * <p>
 * 負責處理會員的新增、修改、刪除、查詢、登入等業務邏輯， 並透過 {@link MemberRepository} 與資料庫進行互動。
 * </p>
 */
@Service("memberService")
public class MemberService {

	@Autowired
	MemberRepository repository;

	public void addMember(MemberVO memberVO) {
		repository.save(memberVO);
	}

	public void updateMember(MemberVO memberVO) {
		repository.save(memberVO);
	}

	public void deleteMember(Integer memberId) {
		if (repository.existsById(memberId))
			repository.deleteByMemberId(memberId);
	}

	/**
	 * 根據會員編號查詢單一會員。
	 *
	 * @param memberId 會員編號。
	 * @return 若存在則回傳 {@link MemberVO}，否則回傳 {@code null}。
	 */
	public MemberVO getOneMember(Integer memberId) {
		Optional<MemberVO> optional = repository.findById(memberId);
		return optional.orElse(null); // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}

	/**
	 * 根據會員帳號查詢單一會員。
	 *
	 * @param account 會員帳號。
	 * @return 若存在則回傳 {@link MemberVO}，否則回傳 {@code null}。
	 */
	public MemberVO getOneMember(String account) {
		Optional<MemberVO> optional = Optional.ofNullable(repository.findByAccount(account));
		return optional.orElse(null); // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}

	public List<MemberVO> getAll() {
		return repository.findAll();
	}

	/**
	 * 初始化會員資料（註冊階段使用）。
	 *
	 * @param account  會員帳號。
	 * @param authcode 驗證碼（同時作為初始密碼）。
	 * @return 初始化後的 {@link MemberVO} 物件。
	 */
	public MemberVO initMember(String account, String authcode) {
		MemberVO initMemVO = new MemberVO();

		initMemVO.setAccount(account);
		initMemVO.setPassword(EncodingUtil.hashMD5(authcode)); // default password (same as authcode)
		initMemVO.setEmailVerified((byte) 0); // 未通過
		initMemVO.setRegisterDate(LocalDateTime.now()); // 註冊時間
		initMemVO.setAccountStatus((byte) 0); // 未啟用
		initMemVO.setEmailAuthToken(authcode); // 產生六位數驗證碼
		initMemVO.setUpdateDate(LocalDateTime.now());

		repository.save(initMemVO); // 新增會員

		return initMemVO;

	}

	/**
	 * 驗證登入（帳號 + 密碼）。
	 *
	 * @param account 帳號。
	 * @param hashedPassword 經加密的密碼。
	 * @return 若登入成功則回傳會員資料，否則回傳 {@code null}。
	 */
	public MemberVO login(String account, String hashedPassword) {
		return repository.findByAccountAndPassword(account, hashedPassword);
	}

	/**
     * 根據帳號查詢會員（登入前驗證用）。
     *
     * @param account 帳號。
     * @return 對應帳號的會員資料，若不存在則回傳 {@code null}。
     */
	public MemberVO login(String account) {
		return repository.findByAccount(account);
	}

}