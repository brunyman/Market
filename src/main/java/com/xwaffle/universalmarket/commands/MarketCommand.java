package com.xwaffle.universalmarket.commands;

import com.xwaffle.universalmarket.UniversalMarket;
import com.xwaffle.universalmarket.market.MarketItem;
import net.craftersland.restarter.RR;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;

/**
 * Created by Chase(Xwaffle) on 12/18/2017.
 */
public class MarketCommand implements CommandExecutor {
    public MarketCommand() {
        //super("", "The Main market Command.", "/market");
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext cmd) throws CommandException {
        //String[] args = arguments.split(" ");

        //Player player = (Player) source;
		String[] args = {};
		if (cmd.<String>getOne("message").isPresent() == true) {
			args = cmd.<String>getOne("message").get().split(" ");
		}

        Player player = null;
        if (source instanceof Player) {
        	//System.out.println("Debug market 2 - ");
            player = (Player) source;
        }
		
        long expireTime = UniversalMarket.getInstance().getMarket().getExpireTime();
        long totalListings = UniversalMarket.getInstance().getMarket().getTotalItemsCanSell();


        /*if (arguments.isEmpty() || arguments.equalsIgnoreCase("")) {
            if (player.hasPermission("com.xwaffle.universalmarket.open")) {
                UniversalMarket.getInstance().getMarket().openMarket(player);
            } else {
                player.sendMessage(Text.of(TextColors.RED, "You do not have permission to view the market."));
            }
            return CommandResult.success();
        }*/

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "open":
                case "o":
				    UniversalMarket.getInstance().getMarket().openMarket(player);
                    /*if (player.hasPermission("com.xwaffle.universalmarket.open")) {
                        UniversalMarket.getInstance().getMarket().openMarket(player);
                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "You do not have permission to view the market."));
                    }*/
                    break;
                case "add":
                case "a":


                    if (!player.hasPermission("com.xwaffle.universalmarket.add")) {
                        player.sendMessage(Text.of(TextColors.RED, "You do not have permission to add items to the market."));
                        return CommandResult.success();
                    }

                    int listingCount = UniversalMarket.getInstance().getMarket().countListings(player.getUniqueId());
                    if (args.length < 2) {
                        player.sendMessage(Text.of(TextColors.RED, "Invalid Command!"));
                        player.sendMessage(Text.of(TextColors.YELLOW, "/um " + args[0].toLowerCase() + " (price of item in hand) (<optional> Amount)"));
                        return CommandResult.success();
                    }

                    /*if (listingCount >= totalListings) {
                        player.sendMessage(Text.of(TextColors.RED, "You are already selling the maximum amount of items at a time."));
                        return CommandResult.success();
                    }*/


                    if (UniversalMarket.getInstance().getMarket().isUsePermissionToSell()) {
                      int userMaxSellPerm = 0;
                      if (player.hasPermission("com.xwaffle.universalmarket.addunlimited")) {
                        userMaxSellPerm = 1000;
                      } else {
                        for (int i = 1; i < 41; i++) {
                          if (player.hasPermission("com.xwaffle.universalmarket.addmax." + i)) {
                            userMaxSellPerm = i;
                          }
                        }
                      }
                      if (userMaxSellPerm <= listingCount) {
                          player.sendMessage(Text.of(TextColors.RED, "You've reached your maximum amount of items you can sell in the market."));
                          player.sendMessage(Text.of(TextColors.RED, "You only have permission to sell ", TextColors.GRAY, userMaxSellPerm, TextColors.RED, " items in the market."));
                          return CommandResult.success();
                      }
                    } else if (listingCount >= totalListings) {
                        player.sendMessage(Text.of(TextColors.RED, "You are already selling the maximum amount of items at a time."));
                        return CommandResult.success();
                    }


                    if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                        ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).get();
                        String name = stack.getType().getName();
                        if (name.contains("fake")) {
                            player.sendMessage(Text.of(TextColors.RED, "You can not sell this item on the market!"));
                            RR.log.info(player.getName() + " just tried to sell a fake item on the market!");
                            return CommandResult.success();
                        }
                        double price;
                        try {
                            price = Double.parseDouble(args[1]);
							if (price < 0) {
                                player.sendMessage(Text.of(TextColors.RED, "You must enter a positive price!"));
                                return CommandResult.success();

                            }
                        } catch (Exception exc) {
                            player.sendMessage(Text.of(TextColors.RED, "Invalid Price for Item!"));
                            player.sendMessage(Text.of(TextColors.YELLOW, "/um " + args[0].toLowerCase() + " (price of item in hand) (<optional> Amount)"));
                            return CommandResult.success();
                        }

                        int amount = stack.getQuantity();

                        if (args.length >= 3) {
                            try {
                                amount = Integer.parseInt(args[2]);
                                if (amount <= 0) {
                                    player.sendMessage(Text.of(TextColors.RED, "You must enter a positive number to sell in the market!"));
                                    return CommandResult.success();
                                }
                            } catch (Exception exc) {
                                player.sendMessage(Text.of(TextColors.RED, "Invalid Amount for Item!"));
                                player.sendMessage(Text.of(TextColors.YELLOW, "/um " + args[0].toLowerCase() + " (price of item in hand) (<optional> Amount)"));
                                return CommandResult.success();
                            }
                        }



                        if (UniversalMarket.getInstance().getMarket().useTax()) {
                            double tax = price * UniversalMarket.getInstance().getMarket().getTax();
                            UniqueAccount account = UniversalMarket.getInstance().getEconomyService().getOrCreateAccount(player.getUniqueId()).get();
                            Currency currency = UniversalMarket.getInstance().getEconomyService().getDefaultCurrency();
                            if (account.getBalance(currency).doubleValue() < tax) {
                                player.sendMessage(Text.of(TextColors.RED, "You can not afford the item tax!"));
                                player.sendMessage(Text.of(TextColors.RED, "You must pay ", TextColors.YELLOW, UniversalMarket.getInstance().getMarket().getTax(), TextColors.RED, " of the item price."));
                                player.sendMessage(Text.of(TextColors.RED, "You need to pay ", TextColors.GREEN, tax, TextColors.RED, " in order to sell this item in the market."));
                                return CommandResult.success();
                            } else {
                                account.withdraw(currency, new BigDecimal(tax), Cause.of(EventContext.empty(), UniversalMarket.getInstance()));
                                player.sendMessage(Text.of(TextColors.RED, "Tax for selling the item has been taken from you!"));
                                player.sendMessage(Text.of(TextColors.DARK_RED, "- $", TextColors.RED, tax));
                            }
                        }

                        if (UniversalMarket.getInstance().getMarket().payFlatPrice()) {
                            double flatPrice = UniversalMarket.getInstance().getMarket().getFlatPrice();
                            UniqueAccount account = UniversalMarket.getInstance().getEconomyService().getOrCreateAccount(player.getUniqueId()).get();
                            Currency currency = UniversalMarket.getInstance().getEconomyService().getDefaultCurrency();
                            if (account.getBalance(currency).doubleValue() < flatPrice) {
                                player.sendMessage(Text.of(TextColors.RED, "You must pay ", TextColors.GRAY, "$" + flatPrice, TextColors.RED, " in order to sell in the market."));
                                return CommandResult.success();
                            } else {
                                account.withdraw(currency, new BigDecimal(flatPrice), Cause.of(EventContext.empty(), UniversalMarket.getInstance()));
                                player.sendMessage(Text.of(TextColors.RED, "A Market fee has been taken out!"));
                                player.sendMessage(Text.of(TextColors.DARK_RED, "- $", TextColors.RED, flatPrice));
                            }
                        }

                        if (UniversalMarket.getInstance().getMarket().isItemBlacklisted(stack.getType())) {
                            player.sendMessage(Text.of(TextColors.RED, "This item cannot be sold (" + stack.getType().getId() + ")"));
                            return CommandResult.success();
                        }


                        int prevAmount = stack.getQuantity();

                        if (amount == stack.getQuantity()) {
                            player.setItemInHand(HandTypes.MAIN_HAND, null);
                        } else {

                            if (amount > stack.getQuantity()) {
                                player.sendMessage(Text.of(TextColors.RED, "You can not sell more than what you're holding."));
                                return CommandResult.success();
                            }

                            stack.setQuantity(amount);
                        }


                        int id = UniversalMarket.getInstance().getDatabase().createEntry(stack.copy(), player.getUniqueId(), player.getName(), price, System.currentTimeMillis() + 1000*60*60*expireTime);
                        UniversalMarket.getInstance().getMarket().addItem(new MarketItem(id, stack.copy(), player.getUniqueId(), player.getName(), price, (System.currentTimeMillis() + 1000*60*60*expireTime)), false);
                        player.sendMessage(Text.of(TextColors.YELLOW, "Item added to ", TextColors.GRAY, "Players Market", TextColors.YELLOW, " for $", TextColors.DARK_AQUA, price));

                        // Log that a market item was added
                        RR.log.info("***********************");
                        RR.log.info("Market Item Added");
                        RR.log.info("Item: " + stack.getType().getName() + " (" + stack.getTranslation().get() + ")");
                        RR.log.info("Seller: " + player.getName());
                        RR.log.info("***********************");

                        if (amount != prevAmount) {
                            stack.setQuantity(prevAmount - amount);
                            player.setItemInHand(HandTypes.MAIN_HAND, stack);
                        }


                    } else {
                        player.sendMessage(Text.of(TextColors.RED, "Place an item in your hand to sell!"));
                    }


                    break;
                case "help":
                case "h":
                case "?":
                    player.sendMessage(Text.of(TextColors.DARK_AQUA, "Players Market Help"));
                    player.sendMessage(Text.of(TextColors.YELLOW, "/market"));
                    player.sendMessage(Text.of(TextColors.YELLOW, "/market a (price) (<optional> amount) or /market add (price) (<optional> amount)", TextColors.GRAY, " - ", TextColors.GREEN, "Sells current held ItemStack for price."));
                    player.sendMessage(Text.of(TextColors.YELLOW, "/market o or /market open", TextColors.GRAY, " - ", TextColors.GREEN, "Open the Market GUI."));
                    break;
            }
        } else {
            UniversalMarket.getInstance().getMarket().openMarket(player);
        }

        return CommandResult.success();
    }
}
