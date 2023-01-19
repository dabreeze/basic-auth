package com.authentication.loan_mandate.service;import com.authentication.loan_mandate.data.LoanMandateGroupDto;import com.authentication.loan_mandate.domain.model.LoanMandate;import org.json.JSONObject;import org.springframework.stereotype.Service;import org.springframework.web.multipart.MultipartFile;import javax.servlet.http.HttpServletResponse;import java.io.IOException;import java.util.List;import java.util.Map;@Servicepublic interface MandateService {    public JSONObject generateMandate();    public void chronJob();    public List<LoanMandate> exportLoansToExcelFormat(HttpServletResponse response) throws IOException;    public Map<String,String> uploadExcelMandateSchedule(MultipartFile file) throws IOException;    List<LoanMandateGroupDto> generateGroupReportForLoanMandate();    List<LoanMandate> generateReportForLoanMandate(String startDate, String endDate, String batchNumber, Integer status);}