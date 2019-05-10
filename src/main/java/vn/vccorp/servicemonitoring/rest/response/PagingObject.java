package vn.vccorp.servicemonitoring.rest.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
public class PagingObject<T> {
    private long number;
    private long size;
    private long totalPages;
    private long totalElements;
    private List<T> content;
    private boolean last;
    private boolean first;

    public static PagingObject convert(Page page) {
        if (page == null) {
            return null;
        }
        PagingObject pagingObject = new PagingObject();
        pagingObject.setContent(page.getContent());
        pagingObject.setFirst(page.isFirst());
        pagingObject.setLast(page.isLast());
        pagingObject.setNumber(page.getNumber());
        pagingObject.setSize(page.getSize());
        pagingObject.setTotalElements(page.getTotalElements());
        pagingObject.setTotalPages(page.getTotalPages());
        return pagingObject;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }
}
