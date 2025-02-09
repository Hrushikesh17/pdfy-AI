import axios from 'axios';

const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB limit

const api = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 300000, // 5 minutes timeout
  maxContentLength: Infinity,
  maxBodyLength: Infinity
});

export const summarizeText = async (pdfFile) => {
  try {
    const formData = new FormData();
    formData.append('file', pdfFile);

    console.log('Uploading file:', {
      name: pdfFile.name,
      size: `${(pdfFile.size / 1024 / 1024).toFixed(2)}MB`,
      type: pdfFile.type
    });

    const response = await api.post('/pdf/summarize', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        console.log(`Upload Progress: ${percentCompleted}%`);
      }
    });

    const responseData = response.data;
    console.log('Raw response:', responseData);

    // Handle direct string response
    if (typeof responseData === 'string') {
      return responseData.trim();
    }

    // Handle array of objects with summary_text
    if (Array.isArray(responseData)) {
      const summaries = responseData
        .filter(item => item && item.summary_text)
        .map(item => item.summary_text.trim());
      
      if (summaries.length > 0) {
        return summaries.join('\n\n');
      }
    }

    // Handle newline-separated JSON strings
    if (typeof responseData === 'string') {
      try {
        const jsonArrays = responseData
          .split('\n')
          .filter(text => text.trim())
          .map(text => JSON.parse(text))
          .flat()
          .filter(item => item && item.summary_text)
          .map(item => item.summary_text.trim());

        if (jsonArrays.length > 0) {
          return jsonArrays.join('\n\n');
        }
      } catch (parseError) {
        console.warn('Failed to parse JSON arrays:', parseError);
        // If parsing fails, return the original string
        return responseData.trim();
      }
    }

    throw new Error('Unable to extract summary from response');

  } catch (error) {
    console.error('Summary error:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      type: typeof error.response?.data
    });
    throw new Error(error.message || 'Failed to get summary');
  }
};

export const answerQuestion = async (file, question) => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('question', question);

    console.log('Sending Q&A request:', {
      fileName: file.name,
      fileSize: `${(file.size / 1024 / 1024).toFixed(2)}MB`,
      question: question
    });

    const response = await api.post('/pdf/answer', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        console.log(`Upload Progress: ${percentCompleted}%`);
      }
    });

    const responseData = response.data;
    console.log('Answer response:', responseData);

    // Handle string response
    if (typeof responseData === 'string') {
      return responseData.trim();
    }

    // Handle object with answer property
    if (responseData && typeof responseData === 'object' && 'answer' in responseData) {
      return responseData.answer.trim();
    }

    throw new Error('Invalid answer format received');

  } catch (error) {
    console.error('Q&A error:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    });
    throw new Error(error.message || 'Failed to get answer');
  }
};