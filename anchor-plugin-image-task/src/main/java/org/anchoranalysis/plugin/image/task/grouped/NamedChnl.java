package org.anchoranalysis.plugin.image.task.grouped;

import org.anchoranalysis.image.channel.Channel;

public class NamedChnl {

	private String name;
	private Channel chnl;
	
	public NamedChnl(String name, Channel chnl) {
		super();
		this.name = name;
		this.chnl = chnl;
	}

	public String getName() {
		return name;
	}

	public Channel getChnl() {
		return chnl;
	}	
	
	
}
