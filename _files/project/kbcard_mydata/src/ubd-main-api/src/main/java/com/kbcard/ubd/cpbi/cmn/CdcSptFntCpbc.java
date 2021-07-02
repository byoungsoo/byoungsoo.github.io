package com.kbcard.ubd.cpbi.cmn;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devonenterprise.business.bm.command.BizCommand;
import devonenterprise.ext.core.exception.LNotFoundException;
import devonenterprise.util.StringUtil;

/**
 * 프로그램명 	: 마이데이터 CDC지원함수
 * 작성자 		: 임용택
 * 작성일자 	: 2021-04-07
 * 설      명 	: 마이데이터 CDC지원함수 cpbi
 */
public class CdcSptFntCpbc {
	
// 고객정보조회
	public LData retvCdcCstInf(LData iCdcCstInf) throws LException {
		LData rCsidInqOut = new LData();
		rCsidInqOut = (LData) BizCommand.execute("com.kbcard.ubd.ebi.card.CdcSptFntEbc", "selectCdcCstInf", iCdcCstInf); 
		return rCsidInqOut;
	}
	
	// 카드식별자조회
	public LData retvCdcCrdIdf(LData iCdcCrdIdf) throws LException {
		LData rCdcCstInf = new LData();
		rCdcCstInf = (LData) BizCommand.execute("com.kbcard.ubd.ebi.card.CdcSptFntEbc", "selectCdcCrdIdf", iCdcCrdIdf); 
		return rCdcCstInf;
	}

	// 카드목록존재여부체크
	public String chkCdcCrdLstExstYn(LData iCdcCrdLst) throws LException {
		LData rCdcCrdLst = new LData();
		String rExstYn = "N";
		rCdcCrdLst = (LData) BizCommand.execute("com.kbcard.ubd.ebi.card.CdcSptFntEbc", "selectCdcCrdLstExstYnInf", iCdcCrdLst); 
		
		if (!StringUtil.trimNisEmpty(rCdcCrdLst.getString("존재여부"))) {
			rExstYn = rCdcCrdLst.getString("존재여부");
		}
		return rExstYn;
	}

	// 청구내역존재여부체크
	public String chkCdcBilHisExstYn(LData iCdcBilHis) throws LException {
		LData tCdcBilHis = new LData();
		tCdcBilHis = (LData) BizCommand.execute("com.kbcard.ubd.ebi.cmn.CdcSptFntEbc", "chkCdcBilHisExstYn", iCdcBilHis); 
		
		String rExstYn = "N";
		rExstYn = tCdcBilHis.getLData("chkCdcBilHisExstYn").toString();
		return rExstYn;
	}
	
	/**
	 * @logicalName 오픈뱅킹사용자등록정보 검증
	 * @param LData  input  입력
	 * 							 -	고객CI_V88
	 * 							 -	이용기관코드_V10
	 * 
	 * @return LData output 결과
	 * 							 -	고객CI_V88
	 * 							 -	이용기관코드_V10
	 * 							 -	고객식별번호
	 * 							 -	고객관리번호
	 * 							 -  사용자유무
	 * @exception LException
	 *                			 - LNotFoundException
	 *                			 - LTooManyRowException
	 * @information 검증 내역 
	 * 							- 오픈뱅킹사용자원장 데이터 유무 
	 * 							- 제3자 정보제공 동의 여부
	 */
//	public LData verifyOpnbUsrRgInf(LData iUsrRgInf) throws LException {
//		
//		LData tUsrRgInf = new LData();
//		UsrMgCpbc usrMgCpbc = new UsrMgCpbc();
//		try {
//			tUsrRgInf.setString("CI내용", iUsrRgInf.getString("고객CI_V88"));
//			tUsrRgInf.setString("오픈뱅킹이용기관코드", iUsrRgInf.getString("이용기관코드_V10"));
//			tUsrRgInf = usrMgCpbc.verifyOpnbUsrRgInf(tUsrRgInf);
//			tUsrRgInf.setString("사용자유무", "Y");
//			tUsrRgInf.setString("고객CI_V88", tUsrRgInf.getString("CI내용"));
//			tUsrRgInf.setString("이용기관코드_V10", tUsrRgInf.getString("오픈뱅킹이용기관코드"));
//			tUsrRgInf.setString("고객식별번호", tUsrRgInf.getString("고객식별자"));
//			tUsrRgInf.setString("고객관리번호", tUsrRgInf.getString("고객관리번호"));
//		} catch (LNotFoundException e) {
//			tUsrRgInf.setString("사용자유무", "N");
//		}
//		
//		/*		이용기관정보에 등록된 서비스 검증 필요한지 유무 
//		LData iVerifyOpnbUtzIns = new LData();
//		String  rExstYn  = null;
//		iVerifyOpnbUtzIns.setString("오픈뱅킹이용기관코드", tUsrRgInf.getString("이용기관코드_V10"));
//		iVerifyOpnbUtzIns.setString("이용기관신청서비스구분코드", tUsrRgInf.getString("거래구분코드_V6"));
//		rExstYn = verifyOpnbUtzIns(iVerifyOpnbUtzIns);
//		
//		if( rExstYn == "Y" ) {
//			
//		}
//		*/
//		
//		return tUsrRgInf;
//		
//	}
	
	// 오픈뱅킹이용기관검증
	// input ---------------------------------------------------------------
	//	 1      오픈뱅킹이용기관코드  			String  10   (전문으로 수신받은 이용기관코드_V10)
	// output --------------------------------------------------------------
	//	 1   	오픈뱅킹이용기관상태구분코드 	String   2   00:등록
	//	    											 	 02:해지
	// ---------------------------------------------------------------------
	// 예제 
	//	CdcSptFntCpbc cdcSptFntCpbc = new CdcSptFntCpbc();	  //이용기관 호출
	//	LData iCdcSptFntCpbc = new LData(); //이용기관 호출 LData 생성
	//	iCdcSptFntCpbc.setString("오픈뱅킹이용기관코드", input.getString("이용기관코드_V10") );
	//	String rStcd = cdcSptFntCpbc.verifyOpnbUtzIns(input); //상태 코드 수신
	//	if(  ! rStcd.equals( UBE_CONST.UTZ_INS_DTCD_NEW ) ) { //"00" 등록
	//		input.setString("응답코드", UBE_CONST.REP_CD_NT_PTCP_INS ); // 미 참가 기관
	//	}
	// ---------------------------------------------------------------------
	public String verifyOpnbUtzIns(LData iUtzIns) throws LException {
		LData tUtzIns = new LData();
		String rStcd = "02";
		
		try {
			tUtzIns.setString("오픈뱅킹이용기관코드", iUtzIns.getString("오픈뱅킹이용기관코드") );
			tUtzIns = (LData) BizCommand.execute("com.kbcard.ubd.ebi.ins.InsBasEbc", "selectOpnbApiIns", tUtzIns); 
			rStcd = tUtzIns.getString("오픈뱅킹이용기관상태구분코드");
		} catch (LNotFoundException e) {
			rStcd = "02";
		}
		
		return rStcd;
	}
	
	/** 요청거래내역 중복 체크 검증
	// input ---------------------------------------------------------------
	//	 1      수신받은 요청 전문  			LData  		(전문으로 수신받은 요청전문)
	// output --------------------------------------------------------------
	//	 1   	bRtn( 거래 응답 )				boolean     (true  : 신규거래)
	//														(false : 중복거래)
	// ---------------------------------------------------------------------
	// 예제 
	//	CdcSptFntCpbc cdcSptFntCpbc = new CdcSptFntCpbc();	  //공통 class 호출
	//	boolean 	  bRtn			= false; 				  //중복요청거래검증 결과 boolean 생성
	//	bRtn = cdcSptFntCpbc.dupDmdTrVln(input); 			  //중복요청거래 검증 결과 수신
	//	if(  ! bRtn ) { //false : 중복거래 시 
	//		throw new LException(); //예외처리 유발
	//	} mydtDmdVlnHisTrUnoDupInq
	*/
	public boolean dupDmdTrVln(LData input) throws LException {
		
		LData iData = new LData();
		LData rData = new LData();
		boolean bRtn = false;
		
		iData.setString("마이데이터전문거래년월일"	, input.getString("거래발생일_V8"	));
		iData.setString("마이데이터거래고유번호"	, input.getString("거래고유번호_V25"));
		iData.setString("오픈API거래식별번호"		, input.getString("거래구분코드_V6"	));
		rData = (LData) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "mydtDmdHisTrUnoDupInq", iData);
		if( rData.isEmpty() ) { //신규
			bRtn = true;
			
		}else { 				//중복
			bRtn = false;
		}
		return bRtn;
	}
		
	/** 요청검증거래내역 중복 체크 검증
	// input ---------------------------------------------------------------
	//	 1      수신받은 요청 전문  			LData  		(전문으로 수신받은 요청전문)
	// output --------------------------------------------------------------
	//	 1   	bRtn( 거래 응답 )				boolean     (true  : 신규거래)
	//														(false : 중복거래)
	// ---------------------------------------------------------------------
	// 예제 
	//	CdcSptFntCpbc cdcSptFntCpbc = new CdcSptFntCpbc();	  //공통 class 호출
	//	boolean 	  bRtn			= false; 				  //중복요청거래검증 결과 boolean 생성
	//	bRtn = cdcSptFntCpbc.dupDmdVlnTrVln(input); 			  //중복요청거래 검증 결과 수신
	//	if(  ! bRtn ) { //false : 중복거래 시 
	//		throw new LException(); //예외처리 유발
	//	} 
	*/
	public boolean dupDmdVlnTrVln(LData input) throws LException {
		
		LData iData = new LData();
		LData rData = new LData();
		boolean bRtn = false;
		
		iData.setString("마이데이터전문거래년월일"	, input.getString("거래발생일_V8"	));
		iData.setString("마이데이터거래고유번호"	, input.getString("거래고유번호_V25"));
		iData.setString("오픈API거래식별번호"		, input.getString("거래구분코드_V6"	));
		rData = (LData) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "mydtDmdVlnHisTrUnoDupInq", iData);
		if( rData.isEmpty() ) { //신규
			bRtn = true;
			
		}else { 				//중복
			bRtn = false;
		}
		return bRtn;
	}		
		
}















