package com.kbcard.ubd.ebi.phs;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devonframework.persistent.autodao.LCommonDao;


/**
 * 프로그램명 	: 마이데이터 요청제공검증내역 query
 * 작성자 		: 임용택
 * 작성일자 	: 2021-06-03
 * 설      명 	: 마이데이터 요청제공검증내역관리 ebi
 */
/**
 * @serviceID 
 * @logicalName 마이데이터 요청제공검증내역 query
 * @param LData input i입력정보
 * @return LData output r결과정보
 * @exception LException.
 * 
 *            ※ 
 */
public class DmdOferVlnHisEbc {
	
	//------------------------------------------------------------------------------------	
	// 마이데이터요청검증내역 등록
	public int insertMydtDmdVlnHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/insertMydtDmdVlnHis",input, dbSpec);
		return dao.executeUpdate();
	}
	// 마이데이터요청검증내역 수정
	public int updateMydtDmdVlnHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/updateMydtDmdVlnHis",input, dbSpec);
		return dao.executeUpdate();
	}	
	// 마이데이터요청검증내역 삭제
	public int deleteMydtDmdVlnHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/deleteMydtDmdVlnHis",input, dbSpec);
		return dao.executeUpdate();	
	}	
	// 마이데이터요청검증내역 조회
	public LMultiData selectMydtDmdVlnHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/selectMydtDmdVlnHis",input, dbSpec);
		return dao.executeQuery();
	}	
	//-----------------------------------------------------------------------------------
	
	//------------------------------------------------------------------------------------	
	// 마이데이터제공검증내역 등록
	public int insertMydtOferVlnHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/insertMydtOferVlnHis",input, dbSpec);
		return dao.executeUpdate();
	}
	// 마이데이터제공검증내역 수정
	public int updateMydtOferVlnHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/updateMydtOferVlnHis",input, dbSpec);
		return dao.executeUpdate();
	}	
	// 마이데이터제공검증내역 삭제
	public int deleteMydtOferVlnHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/deleteMydtOferVlnHis",input, dbSpec);
		return dao.executeUpdate();	
	}	
	// 마이데이터제공검증내역 조회
	public LMultiData selectMydtOferVlnHis(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/selectMydtOferVlnHis",input, dbSpec);
		return dao.executeQuery();
	}	
	//-----------------------------------------------------------------------------------

	//------------------------------------------------------------------------------------	
	// 마이데이터제공검증내역상세 등록
	public int insertMydtOferVlnHisDtl(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/insertMydtOferVlnHisDtl",input, dbSpec);
		return dao.executeUpdate();
	}
	// 마이데이터제공검증내역상세 수정
	public int updateMydtOferVlnHisDtl(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/updateMydtOferVlnHisDtl",input, dbSpec);
		return dao.executeUpdate();
	}	
	// 마이데이터제공검증내역상세 삭제
	public int deleteMydtOferVlnHisDtl(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/deleteMydtOferVlnHisDtl",input, dbSpec);
		return dao.executeUpdate();
	}	
	// 마이데이터제공검증내역상세 조회
	public LMultiData selectMydtOferVlnHisDtl(LData input) throws LException {
		String dbSpec = "default"; // 만약에 log db를 분리한다면, 해당 db connection의 alias를 주면 됨
		LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/selectMydtOferVlnHisDtl",input, dbSpec);
		return dao.executeQuery();
	}	
	//-----------------------------------------------------------------------------------

	// 마이데이터요청내역거래고유번호중복조회
		public LData mydtDmdVlnHisTrUnoDupInq(LData input) throws LException {
			LCommonDao dao = new LCommonDao("phs/DmdOferVlnHisEbc/selectMydtDmdVlnHisTrUnoDupInq",input);
			return dao.executeQueryForSingle();
		}	
}
