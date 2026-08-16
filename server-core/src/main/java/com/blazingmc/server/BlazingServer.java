package com.blazingmc.server;

import com.blazingmc.chat.ConsoleLogger;
import com.blazingmc.protocol.codec.PacketDecoder;
import com.blazingmc.protocol.codec.PacketEncoder;
import com.blazingmc.protocol.handler.MinecraftProtocolHandler;
import com.blazingmc.protocol.handler.ServerInterface;
import com.blazingmc.server.block.BlockManager;
import com.blazingmc.server.block.DoorManager;
import com.blazingmc.server.block.RedstoneManager;
import com.blazingmc.server.block.SignManager;
import com.blazingmc.server.chat.ChatManager;
import com.blazingmc.server.combat.CombatManager;
import com.blazingmc.server.inventory.ContainerManager;
import com.blazingmc.server.inventory.CraftingManager;
import com.blazingmc.server.inventory.FurnaceManager;
import com.blazingmc.server.config.ServerConfig;
import com.blazingmc.server.entity.SpawnManager;
import com.blazingmc.server.entity.ProjectileManager;
import com.blazingmc.server.game.GameManager;
import com.blazingmc.server.obfuscation.OreObfuscator;
import com.blazingmc.server.player.AntiCheatManager;
import com.blazingmc.server.player.EnchantmentManager;
import com.blazingmc.server.player.Player;
import com.blazingmc.server.player.PlayerManager;
import com.blazingmc.server.player.TabListManager;
import com.blazingmc.server.scoreboard.ScoreboardManager;
import com.blazingmc.server.tick.TickScheduler;
import com.blazingmc.world.World;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import java.util.UUID;

public class BlazingServer implements ServerInterface {
    private static BlazingServer instance;
    
    private final ServerConfig config;
    private final TickScheduler tickScheduler;
    private final PlayerManager playerManager;
    private final ChatManager chatManager;
    private final OreObfuscator oreObfuscator;
    private final BlockManager blockManager;
    private final RedstoneManager redstoneManager;
    private final SignManager signManager;
    private final DoorManager doorManager;
    private final AntiCheatManager antiCheatManager;
    private final GameManager gameManager;
    private final TabListManager tabListManager;
    private final SpawnManager spawnManager;
    private final ScoreboardManager scoreboardManager;
    private final CombatManager combatManager;
    private final ContainerManager containerManager;
    private final CraftingManager craftingManager;
    private final FurnaceManager furnaceManager;
    private final EnchantmentManager enchantmentManager;
    private final ProjectileManager projectileManager;
    private final World world;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private volatile boolean running;
    
    public BlazingServer(ServerConfig config) {
        this.config = config;
        this.tickScheduler = new TickScheduler();
        this.playerManager = new PlayerManager(this);
        this.chatManager = new ChatManager(playerManager);
        this.oreObfuscator = new OreObfuscator(config.isAntiXrayEnabled());
        this.blockManager = new BlockManager();
        this.antiCheatManager = new AntiCheatManager(playerManager);
        this.gameManager = new GameManager();
        this.tabListManager = new TabListManager(this);
        this.spawnManager = new SpawnManager(this);
        this.scoreboardManager = new ScoreboardManager();
        this.combatManager = new CombatManager(this);
        this.containerManager = new ContainerManager(this);
        this.craftingManager = new CraftingManager();
        this.furnaceManager = new FurnaceManager();
        this.enchantmentManager = new EnchantmentManager();
        this.projectileManager = new ProjectileManager(this);
        this.world = new World(config.getLevelName(), config.getLevelSeed(), config.getViewDistance());
        this.redstoneManager = new RedstoneManager(world);
        this.signManager = new SignManager(world);
        this.doorManager = new DoorManager(world);
        this.running = false;
        tickScheduler.setServer(this);
        instance = this;
    }
    
    public void start() {
        ConsoleLogger.serverStart("1.0.0", config.getPort());
        running = true;
        
        tickScheduler.start();
        startNetworking();
        
        ConsoleLogger.info("Server started " + ConsoleLogger.RESET);
    }
    
    private void startNetworking() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("packet-encoder", new PacketEncoder());
                        ch.pipeline().addLast("packet-decoder", new PacketDecoder());
                        ch.pipeline().addLast("handler", new MinecraftProtocolHandler(
                            BlazingServer.this,
                            config.isOnlineMode()
                        ));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);
            
            ChannelFuture future = bootstrap.bind(config.getPort()).sync();
            ConsoleLogger.info("Netty server bound to port " + config.getPort());
            
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            ConsoleLogger.error("Netty server interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            shutdownNetworking();
        }
    }
    
    private void shutdownNetworking() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
    
    @Override
    public void onPlayerJoin(ChannelHandlerContext ctx, UUID uuid, String username, Cipher encryptCipher, Cipher decryptCipher) {
        Player player = new Player(this, ctx, uuid, username, encryptCipher, decryptCipher);
        playerManager.addPlayer(player);
    }
    
    @Override
    public void onPlayerDisconnect(UUID uuid) {
        playerManager.removePlayer(uuid);
    }
    
    @Override
    public int getMaxPlayers() {
        return config.getMaxPlayers();
    }
    
    @Override
    public int getOnlinePlayerCount() {
        return playerManager.getOnlinePlayerCount();
    }
    
    @Override
    public String getServerName() {
        return config.getServerName();
    }
    
    @Override
    public String getMotd() {
        return config.getMotd();
    }
    
    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    @Override
    public ChatManager getChatManager() {
        return chatManager;
    }
    
    @Override
    public World getWorld() {
        return world;
    }
    
    @Override
    public BlockManager getBlockManager() {
        return blockManager;
    }
    
    public RedstoneManager getRedstoneManager() {
        return redstoneManager;
    }
    
    public SignManager getSignManager() {
        return signManager;
    }
    
    public DoorManager getDoorManager() {
        return doorManager;
    }
    
    @Override
    public AntiCheatManager getAntiCheatManager() {
        return antiCheatManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public TabListManager getTabListManager() {
        return tabListManager;
    }
    
    public SpawnManager getSpawnManager() {
        return spawnManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public OreObfuscator getOreObfuscator() {
        return oreObfuscator;
    }
    
    public CombatManager getCombatManager() {
        return combatManager;
    }
    
    public ContainerManager getContainerManager() {
        return containerManager;
    }
    
    public CraftingManager getCraftingManager() {
        return craftingManager;
    }
    
    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public FurnaceManager getFurnaceManager() {
        return furnaceManager;
    }

    public EnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }
    
    public void stop() {
        ConsoleLogger.serverStop();
        running = false;
        playerManager.shutdown();
        tickScheduler.stop();
        shutdownNetworking();
        ConsoleLogger.info("Server stopped.");
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public ServerConfig getConfig() {
        return config;
    }
    
    public TickScheduler getTickScheduler() {
        return tickScheduler;
    }
    
    public static BlazingServer getInstance() {
        return instance;
    }
    
    public static void main(String[] args) {
        ServerConfig config = ServerConfig.load();
        BlazingServer server = new BlazingServer(config);
        
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        server.start();
    }
}