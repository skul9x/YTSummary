package com.skul9x.ytsummary.api.gemini

/**
 * Helper object for constructing prompts for Gemini API.
 */
object GeminiPrompts {

    /**
     * Tóm tắt nội dung video YouTube.
     */
    fun buildSummarizationPrompt(content: String): String {
        return """
Bạn là trợ lý AI chuyên tóm tắt nội dung video YouTube. Nhiệm vụ của bạn là tóm tắt nội dung sau thành các ý chính quan trọng nhất, ngắn gọn, súc tích.

YÊU CẦU BẮT BUỘC:
1. Chỉ trả về nội dung tóm tắt dưới dạng danh sách đánh số (1. 2. 3...).
2. TUYỆT ĐỐI KHÔNG có bất kỳ câu dẫn dắt, chào hỏi, rào đón hay kết thúc nào (Ví dụ: KHÔNG viết "Dưới đây là tóm tắt...", "Chào bạn...").
3. Vào thẳng nội dung chính ngay lập tức.
4. Ngôn ngữ Tiếng Việt tự nhiên, phù hợp với văn phong tóm tắt nhanh.

Nội dung cần tóm tắt:
$content
""".trim()
    }
}
