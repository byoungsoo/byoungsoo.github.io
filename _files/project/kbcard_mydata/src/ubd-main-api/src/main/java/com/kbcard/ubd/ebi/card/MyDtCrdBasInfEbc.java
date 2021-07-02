package com.kbcard.ubd.ebi.card;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.DevonException;
import devonframework.persistent.autodao.LCommonDao;

/**
 * 프로그램명 		: 오픈뱅킹API 카드정보조회 query
 * 작성자 			: 박재성
 * 작성일자 		: 2021-04-07 
 * 설      명 		: 오픈뱅킹 카드정보조회 Ebc 
 */
 
public class MyDtCrdBasInfEbc { 
 
	/** 카드기본정보조회 **/
	public LData retvMydtCrdBasInf(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectMyDtApiCrdBasInf", input);
		return dao.executeQueryForSingle();
	}
	/** 카드상품발급조회 **/
	public LMultiData retvLstCrdPdIsu(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectCrdPdIsuInf", input);
		return dao.executeQueryForPage();
	}
	
	/** 처리유형수령부점카드상세중요내역정보조회 **/
	public LData retvPrcPtRceBhCrdDtlImpaHisInf(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectPrcPtRceBhCrdDtlImpaHis", input);
		return dao.executeQueryForSingle();
	}		
		
	/** 카드기능유형존재유효성조회 **/
	public LMultiData retvCrdFuncPtExstVldCfm(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectCrdFuncPtExstVldCfmInf", input);
		return dao.executeQueryForPage();
	}	

	/** 카드연회비정보제공입력 **/
	public LData retvCrdAnfInfOfer(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectCrdAnfInfOferInf", input);
		return dao.executeQueryOnlySingle();
	}		
	
	/** 카드상품일반제휴연회비목록조회 **/
	public LMultiData retvLstCrdPdGnrAliAnf(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectLstCrdPdGnrAliAnfInf", input);
		return dao.executeQueryForPage();
	}		
	
	/** 카드상품가격설계목록조회 **/
	public LMultiData retvLstCrdPdPrePlag(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectLstCrdPdPrePlagInf", input);
		return dao.executeQueryForPage();
	}	
	
	/** 기본연회비기준BY카드브랜드구분코드조회 **/
	public LMultiData retvBasAnfBseCrdBrdDtcd(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectBasAnfBseCrdBrdDtcdInf", input);
		return dao.executeQueryForPage();
	}	
	
	/** 기본연회비기준조회BY조회기준년월일조회 **/
	public LData retvBasAnfBseInqBseYmd(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectBasAnfBseInqBseYmdInf", input);
		return dao.executeQueryOnlySingle();
	}	
	
	/** 카드상품브랜드목록조회 **/
	public LMultiData retvLstCrdPdBrd(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtCrdBasInfEbc/selectLstCrdPdBrdInf", input);
		return dao.executeQueryForPage();
	}		
}










