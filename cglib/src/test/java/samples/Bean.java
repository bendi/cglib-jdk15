package samples;

import java.beans.*;
/**
 *
 * @author  baliuka
 */
public abstract class Bean implements java.io.Serializable{

	private static final long serialVersionUID = -3396864428269221913L;
	String sampleProperty;

  abstract public void addPropertyChangeListener(PropertyChangeListener listener);

  abstract public void removePropertyChangeListener(PropertyChangeListener listener);

   public String getSampleProperty(){
      return sampleProperty;
   }

   public void setSampleProperty(String value){
      this.sampleProperty = value;
   }

   @Override
   public String toString(){
     return "sampleProperty is " + sampleProperty;
   }

}
