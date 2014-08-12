package test.java;

import static org.mockito.Mockito.*;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wetty.httpserver.utils.statistics.ChannelHolder;
import static org.junit.Assert.assertEquals;

public class ChannelHolderTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testShouldIncrementActiveChannelsIfAddingActive() {
		NioSocketChannel mockedChannel = mock(NioSocketChannel.class);

		when(mockedChannel.isActive()).thenReturn(true);

		ChannelHolder.add(mockedChannel);

		assertEquals(ChannelHolder.size(), 1);
	}

	@Test
	public void testShouldDecrementActiveChannelsIfAddingInactive() {
		NioSocketChannel mockedChannel = mock(NioSocketChannel.class);

		when(mockedChannel.isActive()).thenReturn(true);

		ChannelHolder.add(mockedChannel);

		when(mockedChannel.isActive()).thenReturn(false);
		ChannelHolder.remove(mockedChannel);

		assertEquals(ChannelHolder.size(), 0);
	}

}
