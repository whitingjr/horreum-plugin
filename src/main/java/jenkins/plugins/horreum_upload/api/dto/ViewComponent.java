package jenkins.plugins.horreum_upload.api.dto;

import java.util.Objects;

/**
 * Security model: view components are owned by {@link View} and this is owned by {@link HorreumTest}, therefore
 * we don't have to retain ownership info.
 */
public class ViewComponent {

   public Integer id;

   public View view;

   public int headerOrder;

   public String headerName;

   public String accessors;

   /**
    * When this is <code>null</code> defaults to rendering as plain text.
    */
   public String render;

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ViewComponent that = (ViewComponent) o;
      return headerOrder == that.headerOrder &&
            Objects.equals(id, that.id) &&
            Objects.equals(headerName, that.headerName) &&
            Objects.equals(accessors, that.accessors) &&
            Objects.equals(render, that.render);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, headerOrder, headerName, accessors, render);
   }


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public int getHeaderOrder() {
		return headerOrder;
	}

	public void setHeaderOrder(int headerOrder) {
		this.headerOrder = headerOrder;
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getAccessors() {
		return accessors;
	}

	public void setAccessors(String accessors) {
		this.accessors = accessors;
	}

	public String getRender() {
		return render;
	}

	public void setRender(String render) {
		this.render = render;
	}
}
