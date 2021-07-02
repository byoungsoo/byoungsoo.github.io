package com.kbcard.ubd.ebi.phs;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;


/**
 * 프로그램명 	: 마이데이터 요청제공내역 query
 * 작성자 		: 임용택
 * 작성일자 	: 2021-06-03
 * 설      명 	: 마이데이터 요청제공내역관리 ebi
 */
/**
 * @serviceID 
 * @logicalName 마이데이터 요청제공내역 query
 * @param LData input i입력정보
 * @return LData output r결과정보
 * @exception LException.
 * 
 *            ※ 
 */
public class DmdOferHisEbc {
	
	//------------------------------------------------------------------------------------	
	// 마이데이터요청내역등록
	public int insertMydtDmdHis(LData input) throws LException {
        String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/insertMydtDmdHis", input, dbSpec);
		return dao.executeUpdate();
	}
	// 마이데이터요청내역수정
	public int updateMydtDmdHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/updateMydtDmdHis",input, dbSpec);
		return dao.executeUpdate();
	}	
	// 마이데이터요청내역삭제
	public int deleteMydtDmdHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/deleteMydtDmdHis",input, dbSpec);
		return dao.executeUpdate();	
	}	
	// 마이데이터요청내역조회
	public LMultiData selectMydtDmdHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/selectMydtDmdHis",input, dbSpec);
		return dao.executeQuery();
	}	
	//-----------------------------------------------------------------------------------
	
	//------------------------------------------------------------------------------------	
	// 마이데이터제공내역등록
	public int insertMydtOferHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/insertMydtOferHis",input, dbSpec);
		return dao.executeUpdate();
	}
	// 마이데이터제공내역수정
	public int updateMydtOferHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/updateMydtOferHis",input, dbSpec);
		return dao.executeUpdate();
	}	
	// 마이데이터제공내역삭제
	public int deleteMydtOferHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/deleteMydtOferHis",input, dbSpec);
		return dao.executeUpdate();	
	}	
	// 마이데이터제공내역조회
	public LMultiData selectMydtOferHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/selectMydtOferHis",input, dbSpec);
		return dao.executeQuery();
	}	
	//-----------------------------------------------------------------------------------

	//------------------------------------------------------------------------------------	
	// 마이데이터제공내역상세등록
	public int insertMydtOferHisDtl(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/insertMydtOferHisDtl",input, dbSpec);
		return dao.executeUpdate();
	}
	// 마이데이터제공내역상세수정
	public int updateMydtOferHisDtl(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/updateMydtOferHisDtl",input, dbSpec);
		return dao.executeUpdate();
	}	
	// 마이데이터제공내역상세삭제
	public int deleteMydtOferHisDtl(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/deleteMydtOferHisDtl",input, dbSpec);
		return dao.executeUpdate();
	}	
	// 마이데이터제공내역상세조회
	public LMultiData selectMydtOferHisDtl(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/selectMydtOferHisDtl",input, dbSpec);
		return dao.executeQuery();
	}	
	//-----------------------------------------------------------------------------------

	// 마이데이터요청내역거래고유번호중복조회
		public LData mydtDmdHisTrUnoDupInq(LData input) throws LException {
			LCommonDao dao = new LCommonDao("phs/DmdOferHisEbc/selectMydtDmdHisTrUnoDupInq",input);
			return dao.executeQueryForSingle();
		}	
}
