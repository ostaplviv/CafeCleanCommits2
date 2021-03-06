package ua.tag;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class SizeTag extends SimpleTagSupport {

    private final StringWriter sw = new StringWriter();
    private static final String AMPER = "&";
    private static final String QUEST = "?";
    private static final String EQUAL = "=";
    private String title = "Size";
    private int size;
    private int[] posibleSizes;

    @Override
    public void doTag() throws IOException {
        JspWriter out = getJspContext().getOut();
        sw.append("<button class='btn btn-sm btn-outline-success dropdown-toggle' type='button' data-toggle='dropdown'>");
        sw.append(title);
        sw.append("</button><div class='dropdown-menu'>");
        for (int size : posibleSizes) {
            if (size == this.size) {
                sw.append("<a class='dropdown-item active' href='");
            } else {
                sw.append("<a class='dropdown-item' href='");
            }
            sw.append(QUEST);
            sw.append("size=");
            sw.append(String.valueOf(size));
            addAllParameters();
            sw.append("'>");
            sw.append(String.valueOf(size));
            sw.append("</a>");
        }
        sw.append("</div>");
        out.println(sw.toString());
    }

    private void addAllParameters() {
        PageContext pageContext = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Map<String, String[]> map = request.getParameterMap();
        for (Entry<String, String[]> entry : map.entrySet()) {
            for (String value : entry.getValue()) {
                if (!entry.getKey().equals("size")) {
                    sw.append(AMPER);
                    sw.append(entry.getKey());
                    sw.append(EQUAL);
                    sw.append(value);
                }
            }
        }
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setPosibleSizes(String posibleSizes) {
        StringTokenizer tokenizer = new StringTokenizer(posibleSizes, ", ");
        this.posibleSizes = new int[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            this.posibleSizes[i] = Integer.valueOf(tokenizer.nextToken());
            i++;
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }
}