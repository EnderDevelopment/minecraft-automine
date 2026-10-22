-- AutoMiner for Minecraft 1.8.8
-- Auto-mines with a diamond pickaxe (6/3/3), drops items into specified slots, keeps 1 item, and runs in the background.

local function autoMine()
    -- Check if the player has a diamond pickaxe (6/3/3)
    local pickaxe = turtle.getItemDetail(16)
    if not pickaxe or pickaxe.name ~= "minecraft:diamond_pickaxe" or pickaxe.damage ~= 0 then
        print("Diamond pickaxe (6/3/3) not found in slot 16.")
        return
    end
    
    -- Auto-mine
    while true do
        -- Mine forward
        turtle.dig()
        turtle.forward()
        
        -- Drop items into specified slots
        for i = 1, 15 do
            if turtle.getItemCount(i) > 1 then
                turtle.select(i)
                turtle.drop(turtle.getItemCount(i) - 1)
            end
        end
        
        -- Keep 1 item
        if turtle.getItemCount(16) > 1 then
            turtle.select(16)
            turtle.drop(turtle.getItemCount(16) - 1)
        end
        
        -- Run in the background
        os.sleep(0.5)
    end
end

-- Start the auto-miner
autoMine()